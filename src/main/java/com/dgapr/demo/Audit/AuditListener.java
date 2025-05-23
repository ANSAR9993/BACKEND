package com.dgapr.demo.Audit;

import com.dgapr.demo.Model.AuditLog;
import com.dgapr.demo.Model.AuditedEntity;
import com.dgapr.demo.Model.Identifiable;
import com.dgapr.demo.Repository.AuditLogRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PostPersist;
import jakarta.persistence.PreRemove;
import jakarta.persistence.PreUpdate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

@Component
public class AuditListener {

    private static AuditLogRepository repo;
    private static ObjectMapper mapper;
    private static ApplicationEventPublisher publisher;
    // hold the JSON snapshot after loading
    private static final Map<Object, String> originalStateMap = Collections.synchronizedMap(new WeakHashMap<>());

    @Autowired
    public void setRepo(AuditLogRepository repository) {
        AuditListener.repo = repository;
    }

    @Autowired
    public void setPublisher(ApplicationEventPublisher pub) {
        AuditListener.publisher = pub;
    }

    private static ObjectMapper getMapper() {
        if (mapper == null) {
            mapper = new ObjectMapper()
                    .registerModule(new JavaTimeModule())
                    .configure(com.fasterxml.jackson.databind.SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        }
        return mapper;
    }

    private String currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null && auth.isAuthenticated())
                ? auth.getName()
                : "SYSTEM";
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    protected void persistLog(String table, String rowId, String op, String details) {
        // Prevent recursion or side effects for User entity
        // if ("users".equalsIgnoreCase(table)) return;
        AuditLog a = new AuditLog();
        a.setTableName(table);
        a.setRowId(rowId);
        a.setOperation(op);
        a.setModifiedBy(currentUser());
        a.setTimestamp(Instant.now());
        a.setDetails(details == null
                ? ""
                : details.length() > 2000
                ? details.substring(0, 2000)
                : details
        );
        repo.save(a);
    }

    private void publishAuditEvent(String table, String rowId, String op, String details) {
        if (publisher == null) return;
        publisher.publishEvent(new AuditEvent(this, table, rowId, op, details, currentUser()));
    }

    @PostLoad
    public void onLoad(Object entity) {
        if (entity instanceof AuditLog) return; // Prevent recursion
        try {
            String json = getMapper().writeValueAsString(entity);
            originalStateMap.put(entity, json);
        } catch (Exception e) {
            originalStateMap.put(entity, null);
        }
    }

    @PostPersist
    public void onCreate(Object entity) {
        if (entity instanceof AuditLog) return; // Prevent recursion
        String table = entity.getClass().getAnnotation(jakarta.persistence.Table.class).name();
        String id    = (entity instanceof Identifiable)
                ? ((Identifiable<?>)entity).idAsString()
                : "UNKNOWN";
        try {
            String snap = getMapper().writeValueAsString(entity);
            publishAuditEvent(table, id, "CREATE", snap);
        } catch (Exception e) {
            publishAuditEvent(table, id, "CREATE", "ERROR: " + e.getMessage());
        }
    }

    @PreUpdate
    public void onUpdate(Object entity) {
        if (entity instanceof AuditLog) return; // Prevent recursion
        String table = entity.getClass().getAnnotation(jakarta.persistence.Table.class).name();
        String id    = (entity instanceof Identifiable)
                ? ((Identifiable<?>)entity).idAsString()
                : "UNKNOWN";

        String oldJson = originalStateMap.remove(entity);
        if (oldJson == null) oldJson = "{}";

        String newJson;
        try {
            newJson = getMapper().writeValueAsString(entity);
        } catch (Exception e) {
            newJson = "{}";
        }

        Map<String, Object> oldMap = parseJsonToMap(oldJson);
        Map<String, Object> newMap = parseJsonToMap(newJson);
        Map<String, Map<String, Object>> diffs = new LinkedHashMap<>();
        for (String key : newMap.keySet()) {
            Object o = oldMap.get(key), n = newMap.get(key);
            if (!Objects.equals(o, n)) {
                diffs.put(key, Map.of("old", o, "new", n));
            }
        }

        boolean softDel = (entity instanceof AuditedEntity)
                && Boolean.TRUE.equals(((AuditedEntity)entity).getIsDeleted());
        String op = softDel ? "DELETE" : "UPDATE";

        try {
            String details = getMapper().writeValueAsString(diffs);
            publishAuditEvent(table, id, op, details);
        } catch (Exception e) {
            publishAuditEvent(table, id, op, "ERROR_DIFF: " + e.getMessage());
        }
    }

    @PreRemove
    public void onHardDelete(Object entity) {
        if (entity instanceof AuditLog) return; // Prevent recursion
        String table = entity.getClass().getAnnotation(jakarta.persistence.Table.class).name();
        String id    = (entity instanceof Identifiable)
                ? ((Identifiable<?>)entity).idAsString()
                : "UNKNOWN";
        try {
            String snap = getMapper().writeValueAsString(entity);
            publishAuditEvent(table, id, "HARD_DELETE", snap);
        } catch (Exception e) {
            publishAuditEvent(table, id, "HARD_DELETE", "ERROR: " + e.getMessage());
        }
    }

    @EventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleAuditEvent(AuditEvent event) {
        AuditLog a = new AuditLog();
        a.setTableName(event.getTable());
        a.setRowId(event.getRowId());
        a.setOperation(event.getOp());
        a.setModifiedBy(event.getModifiedBy());
        a.setTimestamp(Instant.now());
        a.setDetails(event.getDetails() == null
                ? ""
                : event.getDetails().length() > 2000
                ? event.getDetails().substring(0, 2000)
                : event.getDetails()
        );
        repo.save(a);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> parseJsonToMap(String json) {
        try {
            return getMapper().readValue(json, Map.class);
        } catch (Exception e) {
            return Collections.emptyMap();
        }
    }
}
