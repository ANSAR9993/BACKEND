package com.dgapr.demo.Specification;

import com.dgapr.demo.Model.User.User;
import com.dgapr.demo.Model.User.Role;
import com.dgapr.demo.Model.User.UserStatu;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.NonNull;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.*;

/**
 * Builds dynamic JPA criteria for filtering {@link User} entities.
 * <p>Supports field-specific filters, global search, and conditional soft-delete exclusion.</p>
 *
 * <ul>
 * <li>**Text filters**: username, email, firstname, lastname, idNumber (case-insensitive 'LIKE')</li>
 * <li>**Enum filters**: role ({@link Role}), status ({@link UserStatu}) (exact match)</li>
 * <li>**Date filters**: createdAt, updatedAt (exact match, expects ISO-8601 format)</li>
 * <li>**Global search**: searches across all defined text fields</li>
 * <li>**Soft-delete**: Excludes deleted users by default, unless explicitly filtered by `status=DELETED`</li>
 * </ul>
 */
public class UserSpecification implements Specification<User> {
    private final Map<String, String> filterParams;

    public UserSpecification(Map<String, String> filterParams) {
        this.filterParams = Objects.requireNonNullElse(filterParams, Map.of());
    }

    /**
     * Constructs a combined {@link Predicate} based on provided filter parameters.
     * This method is the core of the Specification, dynamically building the WHERE clause for the query.
     * It applies soft-delete logic, iterates through field-specific filters, and includes a global search.
     *
     * @param root The root of the query, representing the {@link User} entity.
     * @param query The {@link CriteriaQuery} being constructed.
     * @param cb The {@link CriteriaBuilder} used to create individual predicate expressions.
     * @return A {@link Predicate} combining all applicable filter conditions with a logical AND.
     */
    @Override
    public Predicate toPredicate(@NonNull Root<User> root,
                                 CriteriaQuery<?> query,
                                 @NonNull CriteriaBuilder cb) {
        List<Predicate> predicates = new ArrayList<>();

        // Handle soft-delete unless status = DELETED
        applySoftDeleteLogic(predicates, root, cb);

        // Apply field-specific filters
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

        // Apply global search if present
        applyGlobalSearch(predicates, root, cb);

        // Combine all collected predicates with a logical AND.
        return cb.and(predicates.toArray(new Predicate[0]));
    }

    /**
     * Applies soft-delete logic to the query.
     * By default, it adds a predicate to exclude users where `isDeleted` is true.
     * However, if the `status` filter explicitly specifies "DELETED", this soft-delete
     * predicate is not applied, allowing retrieval of deleted users.
     *
     * @param predicates The list of predicates to which the soft-delete condition will be added.
     * @param root The root of the query.
     * @param cb The criteria builder.
     */
    private void applySoftDeleteLogic(List<Predicate> predicates, Root<User> root, CriteriaBuilder cb) {
        String statusFilter = filterParams.get("status");
        if (statusFilter == null || !statusFilter.equalsIgnoreCase("DELETED")) {
            predicates.add(cb.isFalse(root.get("isDeleted")));
        }
    }

    /**
     * Checks if a given filter key should be ignored because it's a pagination, sorting,
     * or global search parameter, rather than a column-specific filter.
     *
     * @param key The filter parameter key.
     * @return {@code true} if the key should be ignored, {@code false} otherwise.
     */
    private boolean shouldIgnoreKey(String key) {
        return key.equals("globalSearch") || key.equals("page") || key.equals("size") || key.startsWith("sort");
    }

    /**
     * Builds a case-insensitive 'LIKE' predicate for text fields.
     * The search term will match if it appears anywhere within the field's value.
     *
     * @param root The root of the query.
     * @param cb The criteria builder.
     * @param key The name of the text field.
     * @param value The search term for the text field.
     * @return A {@link Predicate} for the text field.
     */
    private Predicate buildTextLikePredicate(Root<User> root, CriteriaBuilder cb, String key, String value) {
        return cb.like(cb.lower(root.get(key)), "%" + value.toLowerCase() + "%");
    }

    /**
     * Builds an exact equality predicate for enum fields.
     * Throws a {@link RuntimeException} if the provided string value does not
     * correspond to a valid enum constant.
     *
     * @param root The root of the query.
     * @param cb The criteria builder.
     * @param key The name of the enum field.
     * @param value The string representation of the enum value.
     * @param enumClass The Class object of the enum type.
     * @param <E> The enum type.
     * @return A {@link Predicate} for the enum field.
     * @throws RuntimeException if the enum value is invalid.
     */
    private <E extends Enum<E>> Predicate buildEnumPredicate(Root<User> root, CriteriaBuilder cb, String key, String value, Class<E> enumClass) {
        try {
            E enumValue = Enum.valueOf(enumClass, value.toUpperCase());
            return cb.equal(root.get(key), enumValue);
        } catch (IllegalArgumentException ex) {
            throw new RuntimeException("Invalid " + key + " value: " + value, ex);
        }
    }

    /**
     * Builds an exact equality predicate for date fields (represented as {@link Instant}).
     * Expects the date value to be in ISO-8601 format (e.g., "2024-05-24T10:00:00Z").
     * Throws a {@link RuntimeException} if the date format is invalid.
     *
     * @param root The root of the query.
     * @param cb The criteria builder.
     * @param key The name of the date field.
     * @param value The string representation of the date.
     * @return A {@link Predicate} for the date field.
     * @throws RuntimeException if the date format is invalid.
     */
    private Predicate buildDatePredicate(Root<User> root, CriteriaBuilder cb, String key, String value) {
        try {
            Instant instant = Instant.parse(value);
            return cb.equal(root.get(key), instant);
        } catch (DateTimeParseException ex) {
            throw new RuntimeException("Invalid date format for " + key + ": " + value, ex);
        }
    }

    /**
     * Applies a global search predicate across multiple text fields.
     * The search term will match if it appears (case-insensitively) in any of the specified fields.
     *
     * @param predicates The list of predicates to which the global search condition will be added.
     * @param root The root of the query.
     * @param cb The criteria builder.
     */
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
