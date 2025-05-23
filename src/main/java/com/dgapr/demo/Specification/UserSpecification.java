package com.dgapr.demo.Specification;

import com.dgapr.demo.Model.User;
import com.dgapr.demo.Model.Role;
import com.dgapr.demo.Model.UserStatu;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.NonNull;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.*;

public class UserSpecification implements Specification<User> {
    private final Map<String, String> filterParams;

    public UserSpecification(Map<String, String> filterParams) {
        this.filterParams = Objects.requireNonNullElse(filterParams, Map.of());
    }

    @Override
    public Predicate toPredicate(@NonNull Root<User> root,
                                 CriteriaQuery<?> query,
                                 @NonNull CriteriaBuilder cb) {
        List<Predicate> predicates = new ArrayList<>();

        // 1. Handle soft-delete unless status = DELETED
        applySoftDeleteLogic(predicates, root, cb);

        // 2. Apply field-specific filters
        filterParams.forEach((key, value) -> {
            if (shouldIgnoreKey(key) || !StringUtils.hasText(value)) return;
            switch (key) {
                case "username":
                case "email":
                case "firstname":
                case "lastname":
                case "idNumber":
                    predicates.add(buildTextLikePredicate(root, cb, key, value));
                    break;
                case "role":
                    predicates.add(buildEnumPredicate(root, cb, key, value, Role.class));
                    break;
                case "status":
                    predicates.add(buildEnumPredicate(root, cb, key, value, UserStatu.class));
                    break;
                case "createdAt":
                case "updatedAt":
                    predicates.add(buildDatePredicate(root, cb, key, value));
                    break;
            }
        });

        // 3. Apply global search if present
        applyGlobalSearch(predicates, root, cb);

        return cb.and(predicates.toArray(new Predicate[0]));
    }

    private void applySoftDeleteLogic(List<Predicate> predicates, Root<User> root, CriteriaBuilder cb) {
        String statusFilter = filterParams.get("status");
        if (statusFilter == null || !statusFilter.equalsIgnoreCase("DELETED")) {
            predicates.add(cb.isFalse(root.get("isDeleted")));
        }
    }

    private boolean shouldIgnoreKey(String key) {
        return key.equals("globalSearch") || key.equals("page") || key.equals("size") || key.startsWith("sort");
    }

    private Predicate buildTextLikePredicate(Root<User> root, CriteriaBuilder cb, String key, String value) {
        return cb.like(cb.lower(root.get(key)), "%" + value.toLowerCase() + "%");
    }

    private <E extends Enum<E>> Predicate buildEnumPredicate(Root<User> root, CriteriaBuilder cb, String key, String value, Class<E> enumClass) {
        try {
            E enumValue = Enum.valueOf(enumClass, value.toUpperCase());
            return cb.equal(root.get(key), enumValue);
        } catch (IllegalArgumentException ex) {
            throw new RuntimeException("Invalid " + key + " value: " + value, ex);
        }
    }

    private Predicate buildDatePredicate(Root<User> root, CriteriaBuilder cb, String key, String value) {
        try {
            Instant instant = Instant.parse(value);
            return cb.equal(root.get(key), instant);
        } catch (DateTimeParseException ex) {
            throw new RuntimeException("Invalid date format for " + key + ": " + value, ex);
        }
    }

    private void applyGlobalSearch(List<Predicate> predicates, Root<User> root, CriteriaBuilder cb) {
        String global = filterParams.get("globalSearch");
        if (StringUtils.hasText(global)) {
            String term = "%" + global.toLowerCase() + "%";
            predicates.add(cb.or(
                    cb.like(cb.lower(root.get("username")), term),
                    cb.like(cb.lower(root.get("email")), term),
                    cb.like(cb.lower(root.get("firstname")), term),
                    cb.like(cb.lower(root.get("lastname")), term),
                    cb.like(cb.lower(root.get("idNumber")), term)
            ));
        }
    }
}
