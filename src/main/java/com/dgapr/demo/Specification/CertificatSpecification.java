package com.dgapr.demo.Specification;

import com.dgapr.demo.Model.Certificate;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CertificatSpecification implements Specification<Certificate> {

    private final Map<String, String> filterParams;

    public CertificatSpecification(Map<String, String> filterParams) {
        this.filterParams = filterParams;
    }

    @Override
    public Predicate toPredicate(
            Root<Certificate> root,
            CriteriaQuery<?> query,
            CriteriaBuilder cb) {

        List<Predicate> predicates = new ArrayList<>();

        // --- Soft-delete: include only non-deleted users ---
        predicates.add(cb.isFalse(root.get("isDeleted")));

        // --- Global Search ---
        String globalSearchTerm = filterParams.get("globalSearch");
        if (StringUtils.hasText(globalSearchTerm)) {
            String term = "%" + globalSearchTerm.toLowerCase() + "%";
            predicates.add(
                    cb.or(
                            cb.like(cb.lower(root.get("idDemand")), term),
                            cb.like(cb.lower(root.get("demandeName")), term),
                            cb.like(cb.lower(root.get("model")), term),
                            cb.like(cb.lower(root.get("type")), term),
                            cb.like(cb.lower(root.get("organizationalUnit")), term),
                            cb.like(cb.lower(root.get("commonName")), term)
                    )
            );
        }

        // --- Column Specific Filtering ---
        filterParams.forEach((key, value) -> {
            if (!key.equals("globalSearch")
                    && !key.equals("page")
                    && !key.equals("size")
                    && !key.startsWith("sort")
                    && !key.equals("isDeleted")
                    && StringUtils.hasText(value)) {

                if (isTextField(key)) {
                    predicates.add(
                            cb.like(
                                    cb.lower(root.get(key)),
                                    "%" + value.toLowerCase() + "%"
                            )
                    );

                } else if (isDateField(key)) {
                    try {
                        LocalDate date = LocalDate.parse(value);
                        predicates.add(cb.equal(root.get(key), date));
                    } catch (DateTimeParseException e) {
                        System.err.println("Invalid date format for filter '" + key + "': '" + value + "'. Expected YYYY-MM-DD. Error: " + e.getMessage());
                    }

                } else if (isNumericField(key)) {
                    try {
                        Long num = Long.valueOf(value);
                        predicates.add(cb.equal(root.get(key), num));
                    } catch (NumberFormatException e) {
                        System.err.println("Invalid number for filter '" + key + "': " + value);
                    }
                }
            }
        });

        return cb.and(predicates.toArray(new Predicate[0]));
    }

    private boolean isTextField(String fieldName) {
        return fieldName.equals("idDemand")
                || fieldName.equals("demandeName")
                || fieldName.equals("model")
                || fieldName.equals("type")
                || fieldName.equals("organizationalUnit")
                || fieldName.equals("commonName");
    }

    private boolean isDateField(String fieldName) {
        return fieldName.equals("creationDate")
                || fieldName.equals("expirationDate");
    }

    private boolean isNumericField(String fieldName) {
        // Add your numeric field names here
        return fieldName.equals("idDemand")   // if numeric
                || fieldName.equals("someNumericField");
    }
}