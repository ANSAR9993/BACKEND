package com.dgapr.demo.Specification;

import com.dgapr.demo.Model.Certificate.Certificate;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.NonNull; // Import for Spring's @NonNull annotation
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects; // Import for Objects.requireNonNullElse

/**
 * A Spring Data JPA {@link Specification} for the {@link Certificate} entity.
 * This class dynamically builds predicates for filtering certificates based on provided
 * request parameters, supporting global search, column-specific filtering, and soft-delete status.
 *
 * <ul>
 * <li>**Text filters**: idDemand, demandeName, model, type, organizationalUnit, commonName (case-insensitive 'LIKE')</li>
 * <li>**Date filters**: creationDate, expirationDate (exact match, expects 'YYYY-MM-DD' format)</li>
 * <li>**Global search**: searches across all defined text fields</li>
 * <li>**Soft-delete**: Excludes deleted certificates by default, unless explicitly requested.</li>
 * </ul>
 */
public class CertificatSpecification implements Specification<Certificate> {

    private final Map<String, String> filterParams;

    public CertificatSpecification(Map<String, String> filterParams) {
        this.filterParams = Objects.requireNonNullElse(filterParams, Map.of());
    }

    /**
     * Creates a {@link Predicate} for the given {@link CriteriaQuery} and {@link CriteriaBuilder}.
     * This method is called by Spring Data JPA to construct the WHERE clause of the query.
     * It combines conditions for soft-deletion, global search, and individual column filters.
     *
     * @param root The root type in the FROM clause, representing the {@link Certificate} entity.
     * @param query The query being constructed. Not directly used for adding predicates, but part of the API.
     * @param cb The criteria builder, used to construct individual predicates (e.g., equals, like, and, or).
     * @return A {@link Predicate} that combines all filtering conditions with a logical AND.
     * @throws IllegalArgumentException if an invalid date format is provided for a date filter.
     */
    @Override
    public Predicate toPredicate(@NonNull Root<Certificate> root,
                                 CriteriaQuery<?> query,
                                 @NonNull CriteriaBuilder cb) {
        List<Predicate> predicates = new ArrayList<>();

        // Apply soft-delete logic: by default, only include non-deleted certificates.
        applySoftDeleteLogic(predicates, root, cb);

        // Apply field-specific filters by iterating through the filter parameters.
        applyFieldSpecificFilters(predicates, root, cb);

        // Apply global search.
        applyGlobalSearch(predicates, root, cb);

        // Combine all collected predicates with a logical AND.
        return cb.and(predicates.toArray(new Predicate[0]));
    }

    /**
     * Applies soft-delete logic to the query.
     * By default, it adds a predicate to exclude certificates where {@code isDeleted} is true.
     * This ensures that only active (non-deleted) certificates are retrieved.
     *
     * @param predicates The list of predicates to which the soft-delete condition will be added.
     * @param root The root of the query.
     * @param cb The criteria builder.
     */
    private void applySoftDeleteLogic(List<Predicate> predicates, Root<Certificate> root, CriteriaBuilder cb) {
        predicates.add(cb.isFalse(root.get("isDeleted")));
    }

    /**
     * Applies column-specific filters based on the provided {@code filterParams}.
     * Iterates through the parameters, identifies field types, and builds appropriate predicates.
     *
     * @param predicates The list of predicates to which field-specific conditions will be added.
     * @param root The root of the query.
     * @param cb The criteria builder.
     * @throws IllegalArgumentException if an invalid date format is provided for a date filter.
     */
    private void applyFieldSpecificFilters(List<Predicate> predicates, Root<Certificate> root, CriteriaBuilder cb) {
        filterParams.forEach((key, value) -> {
            // Exclude non-filter parameters like pagination, sorting, global search, and already handled 'isDeleted'.
            if (shouldIgnoreKey(key) || !StringUtils.hasText(value)) {
                return;
            }

            switch (key) {
                // Text fields: apply case-insensitive 'LIKE' (contains) filter
                case "idDemand":
                case "demandeName":
                case "model":
                case "type":
                case "organizationalUnit":
                case "commonName":
                    predicates.add(buildTextLikePredicate(root, cb, key, value));
                    break;
                // Date fields: attempt to parse LocalDate and apply exact match filter
                case "creationDate":
                case "expirationDate":
                    predicates.add(buildDatePredicate(root, cb, key, value));
                    break;
            }
        });
    }

    /**
     * Checks if a given filter key should be ignored because it's a pagination, sorting,
     * or global search parameter, or a key explicitly handled elsewhere (like 'isDeleted').
     *
     * @param key The filter parameter key.
     * @return {@code true} if the key should be ignored, {@code false} otherwise.
     */
    private boolean shouldIgnoreKey(String key) {
        return key.equals("globalSearch")
                || key.equals("page")
                || key.equals("size")
                || key.startsWith("sort")
                || key.equals("isDeleted");
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
    private Predicate buildTextLikePredicate(Root<Certificate> root, CriteriaBuilder cb, String key, String value) {
        return cb.like(cb.lower(root.get(key)), "%" + value.toLowerCase() + "%");
    }

    /**
     * Builds an exact equality predicate for date fields.
     * Expects the date value to be in 'YYYY-MM-DD' format.
     * Throws an {@link IllegalArgumentException} if the date format is invalid.
     *
     * @param root The root of the query.
     * @param cb The criteria builder.
     * @param key The name of the date field.
     * @param value The string representation of the date.
     * @return A {@link Predicate} for the date field.
     * @throws IllegalArgumentException if the date string cannot be parsed into a {@link LocalDate}.
     */
    private Predicate buildDatePredicate(Root<Certificate> root, CriteriaBuilder cb, String key, String value) {
        try {
            LocalDate date = LocalDate.parse(value);
            return cb.equal(root.get(key), date);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date format for filter '" + key + "': '" + value + "'. Expected YYYY-MM-DD.", e);
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
    private void applyGlobalSearch(List<Predicate> predicates, Root<Certificate> root, CriteriaBuilder cb) {
        String globalSearchTerm = filterParams.get("globalSearch");
        if (StringUtils.hasText(globalSearchTerm)) {
            String term = "%" + globalSearchTerm.toLowerCase() + "%";
            predicates.add(cb.or( // Combine global search conditions with OR
                    cb.like(cb.lower(root.get("idDemand")), term),
                    cb.like(cb.lower(root.get("demandeName")), term),
                    cb.like(cb.lower(root.get("model")), term),
                    cb.like(cb.lower(root.get("type")), term),
                    cb.like(cb.lower(root.get("organizationalUnit")), term),
                    cb.like(cb.lower(root.get("commonName")), term)
            ));
        }
    }
}