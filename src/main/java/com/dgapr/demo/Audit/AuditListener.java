package com.dgapr.demo.Audit;

import com.dgapr.demo.Model.AuditLog;
import com.dgapr.demo.Model.SoftDeletableEntity;
import com.dgapr.demo.Model.Identifiable;
import com.dgapr.demo.Repository.AuditLogRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PostPersist;
import jakarta.persistence.PreRemove;
import jakarta.persistence.PreUpdate;
import lombok.extern.slf4j.Slf4j;
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

/**
 * JPA Entity Listener responsible for capturing and logging audit events for entities.
 * This listener integrates with the Spring application context to publish {@link AuditEvent}s,
 * which are then processed by an {@link EventListener} to persist {@link AuditLog} entries.
 *
 * <p>It intercepts JPA lifecycle events (PostLoad, PostPersist, PreUpdate, PreRemove)
 * to record creation, updates (including diffs), and deletion of entities.</p>
 *
 * <p>The listener uses a {@link WeakHashMap} to store a snapshot of entities after loading
 * to facilitate diffing during updates.</p>
 */
@Slf4j
@Component
public class AuditListener {

    private static AuditLogRepository repo;
    private static ObjectMapper mapper;
    private static ApplicationEventPublisher publisher;

    /**
     * A map to hold the JSON snapshot of an entity after it is loaded (PostLoad),
     * used for calculating differences during updates.
     * Uses a WeakHashMap to allow garbage collection of entities no longer strongly referenced.
     */
    private static final Map<Object, String> originalStateMap = Collections.synchronizedMap(new WeakHashMap<>());

    /**
     * Sets the {@link AuditLogRepository} via dependency injection.
     * This is required because JPA listeners are not standard Spring beans.
     * @param repository The {@link AuditLogRepository} instance.
     */
    @Autowired
    public void setRepo(AuditLogRepository repository) {
        AuditListener.repo = repository;
    }

    /**
     * Sets the {@link ApplicationEventPublisher} via dependency injection.
     * Used to publish audit events to the Spring application context.
     * @param pub The {@link ApplicationEventPublisher} instance.
     */
    @Autowired
    public void setPublisher(ApplicationEventPublisher pub) {
        AuditListener.publisher = pub;
    }

    /**
     * Provides a singleton instance of {@link ObjectMapper} configured for JSON serialization,
     * including support for Java 8 Date and Time API.
     * @return The configured {@link ObjectMapper}.
     */
    private static ObjectMapper getMapper() {
        if (mapper == null) {
            mapper = new ObjectMapper()
                    .registerModule(new JavaTimeModule())
                    .configure(com.fasterxml.jackson.databind.SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        }
        return mapper;
    }

    /**
     * Retrieves the username of the currently authenticated user from the Spring Security context.
     * Returns "SYSTEM" if no user is authenticated.
     * @return The username of the current user or "SYSTEM".
     */
    private String currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null && auth.isAuthenticated())
                ? auth.getName()
                : "SYSTEM";
    }

    /**
     * Persists an audit log entry directly to the database.
     * This method is transactional with {@code REQUIRES_NEW} propagation to ensure
     * that audit logs are saved even if the primary transaction fails.
     * This method is protected as it's typically called internally or by event listeners.
     *
     * @param table   The name of the table affected.
     * @param rowId   The ID of the affected row.
     * @param op      The operation type.
     * @param details Optional details about the operation.
     * @deprecated This method is deprecated in favor of {@link #handleAuditEvent(AuditEvent)}
     * which processes events published by this listener.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Deprecated
    protected void persistLog(String table, String rowId, String op, String details) {

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

    /**
     * Publishes an {@link AuditEvent} to the Spring application context.
     * This is the preferred way to trigger audit logging.
     * If AuditContext indicates that auditing is disabled, this method will not publish an event.
     *
     * @param table   The name of the table affected.
     * @param rowId   The ID of the affected row.
     * @param op      The operation type.
     * @param details Optional details about the operation.
     */
    private void publishAuditEvent(String table, String rowId, String op, String details) {
        if (com.dgapr.demo.Audit.AuditContext.isAuditDisabled()) {
            log.debug("Audit event publication skipped (but core auditing proceeds) for table={}, rowId={}, op={}", table, rowId, op);
            return;
        }

        if (publisher == null) {
            log.warn("ApplicationEventPublisher is null. Audit event for table={}, rowId={}, op={} will not be published.", table, rowId, op);
            return;
        }

        publisher.publishEvent(new AuditEvent(this, table, rowId, op, details, currentUser()));
    }


    /**
     * JPA callback method executed after an entity is loaded from the database.
     * It takes a JSON snapshot of the entity's state and stores it in {@link #originalStateMap}
     * for later use in detecting changes during updates.
     *
     * @param entity The entity that was loaded.
     */
    @PostLoad
    public void onLoad(Object entity) {
        if (entity instanceof AuditLog) return; // Prevent recursion when loading audit logs themselves
        try {
            String json = getMapper().writeValueAsString(entity);
            originalStateMap.put(entity, json);
        } catch (Exception e) {
            originalStateMap.put(entity, null);
        }
    }

    /**
     * JPA callback method executed after an entity is persisted (created) in the database.
     * Publishes an "CREATE" {@link AuditEvent} with the full snapshot of the created entity.
     *
     * @param entity The entity that was just created.
     */
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

    /**
     * JPA callback method executed before an entity is updated in the database.
     * Calculates the differences between the original (loaded) state and the new state of the entity.
     * Publishes an "UPDATE" {@link AuditEvent} with the calculated diffs.
     * If the entity is an {@link SoftDeletableEntity} and marked as deleted, the operation is logged as "DELETE".
     *
     * @param entity The entity that is about to be updated.
     */
    @PreUpdate
    public void onUpdate(Object entity) {
        if (entity instanceof AuditLog) return; // Prevent recursion
        String table = entity.getClass().getAnnotation(jakarta.persistence.Table.class).name();
        String id    = (entity instanceof Identifiable)
                ? ((Identifiable<?>)entity).idAsString()
                : "UNKNOWN";

        // Retrieve original state and remove from map as it's no longer 'original' for subsequent updates
        String oldJson = originalStateMap.remove(entity);
        if (oldJson == null) oldJson = "{}";

        String newJson;
        try {
            newJson = getMapper().writeValueAsString(entity);
        } catch (Exception e) {
            newJson = "{}";
        }

        // Parse JSON strings to maps for comparison
        Map<String, Object> oldMap = parseJsonToMap(oldJson);
        Map<String, Object> newMap = parseJsonToMap(newJson);
        Map<String, Map<String, Object>> diffs = new LinkedHashMap<>();

        // Calculate differences: iterate over newMap keys to find changed values
        for (String key : newMap.keySet()) {
            Object o = oldMap.get(key), n = newMap.get(key);
            if (!Objects.equals(o, n)) {
                diffs.put(key, Map.of("old", o, "new", n));
            }
        }

        // Determine operation type: "DELETE" if soft-deleted, otherwise "UPDATE"
        boolean softDel = (entity instanceof SoftDeletableEntity)
                && Boolean.TRUE.equals(((SoftDeletableEntity)entity).getIsDeleted());
        String op = softDel ? "DELETE" : "UPDATE";

        try {
            String details = getMapper().writeValueAsString(diffs);
            publishAuditEvent(table, id, op, details);
        } catch (Exception e) {
            publishAuditEvent(table, id, op, "ERROR_DIFF: " + e.getMessage());
        }
    }

    /**
     * JPA callback method executed before an entity is physically removed (hard deleted) from the database.
     * Publishes a "HARD_DELETE" {@link AuditEvent} with a snapshot of the entity before deletion.
     * Note: This listener is primarily for entities that are *not* soft-deleted, or for cases
     * where an entity is truly removed.
     *
     * @param entity The entity that is about to be hard deleted.
     */
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

    /**
     * Spring event listener that handles {@link AuditEvent}s published by this class.
     * This method is responsible for persisting the {@link AuditLog} entry to the database.
     * It is transactional with {@code REQUIRES_NEW} propagation to ensure that the audit log
     * is saved independently of the main transaction, preventing audit loss on main transaction rollback.
     *
     * @param event The {@link AuditEvent} to be handled.
     */
    @EventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleAuditEvent(com.dgapr.demo.Audit.AuditEvent event) {
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

    /**
     * Helper method to parse a JSON string into a {@link Map}.
     * Used internally for comparing entity states during updates.
     *
     * @param json The JSON string to parse.
     * @return A {@link Map} representing the JSON data, or an empty map if parsing fails.
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> parseJsonToMap(String json) {
        try {
            return getMapper().readValue(json, Map.class);
        } catch (Exception e) {
            return Collections.emptyMap();
        }
    }
}
