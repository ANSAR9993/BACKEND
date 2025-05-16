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
import org.springframework.stereotype.Component;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.Instant;
import java.util.*;

@Component
public class AuditListener {

    private static AuditLogRepository repo;
    private static ObjectMapper mapper;
    // hold the JSON snapshot after loading
    private static final Map<Object, String> originalStateMap = Collections.synchronizedMap(new WeakHashMap<>());

    @Autowired
    public void setRepo(AuditLogRepository repository) {
        AuditListener.repo = repository;
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

    private void persistLog(String table, String rowId, String op, String details) {
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

    @PostLoad
    public void onLoad(Object entity) {
        try {
            String json = getMapper().writeValueAsString(entity);
            originalStateMap.put(entity, json);
        } catch (Exception e) {
            originalStateMap.put(entity, null);
        }
    }

    @PostPersist
    public void onCreate(Object entity) {
        String table = entity.getClass().getAnnotation(jakarta.persistence.Table.class).name();
        String id    = (entity instanceof Identifiable)
                ? ((Identifiable<?>)entity).idAsString()
                : "UNKNOWN";
        try {
            String snap = getMapper().writeValueAsString(entity);
            persistLog(table, id, "CREATE", snap);
        } catch (Exception e) {
            persistLog(table, id, "CREATE", "ERROR: " + e.getMessage());
        }
    }

    @PreUpdate
    public void onUpdate(Object entity) {
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
            persistLog(table, id, op, details);
        } catch (Exception e) {
            persistLog(table, id, op, "ERROR_DIFF: " + e.getMessage());
        }
    }

    @PreRemove
    public void onHardDelete(Object entity) {
        String table = entity.getClass().getAnnotation(jakarta.persistence.Table.class).name();
        String id    = (entity instanceof Identifiable)
                ? ((Identifiable<?>)entity).idAsString()
                : "UNKNOWN";
        try {
            String snap = getMapper().writeValueAsString(entity);
            persistLog(table, id, "HARD_DELETE", snap);
        } catch (Exception e) {
            persistLog(table, id, "HARD_DELETE", "ERROR: " + e.getMessage());
        }
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
