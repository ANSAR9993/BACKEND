package com.dgapr.demo.Model;

import java.io.Serializable;
import java.util.Optional;

/**
 * Interface for entities that can be identified by a unique ID.
 * <p>
 * Provides a method to retrieve the entity's ID and a default method to get the ID as a String.
 * </p>
 *
 * @param <T> the type of the identifier, which must be Serializable
 */
public interface Identifiable<T extends Serializable> {
    /**
     * @return the ID of the entity, or null if not set
     */
    T getId();

    /**
     * @return the ID as a String, or "UNKNOWN" if the ID is null
     */
    default String idAsString() {
        return Optional.ofNullable(getId()).map(Object::toString).orElse("UNKNOWN");
    }
}
