package com.dgapr.demo.Audit;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AuditContext {

    private static final ThreadLocal<Boolean> auditDisabledFlag = new ThreadLocal<>();

    /**
     * Disables auditing for the current thread.
     */
    public static void disableAudit() {
        auditDisabledFlag.set(Boolean.TRUE);
    }

    /**
     * Clears the audit-disabled flag for the current thread, re-enabling auditing
     * (or reverting to the default state).
     */
    public static void clear() {
        if (auditDisabledFlag.get() == null) {
            log.warn("AuditContext.clear() was called but auditing was not disabled. Possible mismanagement?");
        }
        auditDisabledFlag.remove();
    }

    /**
     * Checks if auditing is currently disabled for the current thread.
     * @return {@code true} if auditing is disabled, {@code false} otherwise.
     */
    public static boolean isAuditDisabled() {
        Boolean isDisabled = auditDisabledFlag.get();
        return Boolean.TRUE.equals(isDisabled);
    }
}