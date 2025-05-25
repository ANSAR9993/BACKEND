// package com.dgapr.demo.Exception;
// File: DuplicateCertificateException.java

package com.dgapr.demo.Exception;

/**
 * Custom exception to indicate that a certificate with conflicting unique data
 * (e.g., idDemand, commonName if unique) already exists.
 * This extends RuntimeException, making it an unchecked exception.
 */
public class DuplicateCertificateException extends RuntimeException {

    /**
     * Constructs a new DuplicateCertificateException with the specified detail message.
     *
     * @param message The detail message (which can be retrieved by the Throwable.getMessage() method).
     */
    public DuplicateCertificateException(String message) {
        super(message);
    }

    /**
     * Constructs a new DuplicateCertificateException with the specified detail message and
     * cause.
     *
     * @param message The detail message.
     * @param cause The cause (which is saved for later retrieval by the Throwable.getCause() method).
     * (A null value is permitted, and indicates that the cause is nonexistent or unknown.)
     */
    public DuplicateCertificateException(String message, Throwable cause) {
        super(message, cause);
    }
}