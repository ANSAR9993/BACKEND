// src/main/java/com/dgapr/demo/Model/Identifiable.java
package com.dgapr.demo.Model;

import java.io.Serializable;
import java.util.Optional;

public interface Identifiable<T extends Serializable> {
    T getId();

    default String idAsString() {
        return Optional.ofNullable(getId()).map(Object::toString).orElse("UNKNOWN");
    }
}
