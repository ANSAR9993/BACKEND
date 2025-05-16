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
            CriteriaBuilder criteriaBuilder)
    {
        List<Predicate> predicates = new ArrayList<>();

        // Filter for active certificates
        predicates.add(criteriaBuilder.isFalse(root.get("isDeleted")));


        // --- Global Search ---
        String globalSearchTerm = filterParams.get("globalSearch");
        if (StringUtils.hasText(globalSearchTerm)) {
            String lowerCaseSearchTerm = "%" + globalSearchTerm.toLowerCase() + "%";
            Predicate globalPredicate = criteriaBuilder.or(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("idDemand")), lowerCaseSearchTerm),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("demandeName")), lowerCaseSearchTerm),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("model")), lowerCaseSearchTerm),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("type")), lowerCaseSearchTerm),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("organizationalUnit")), lowerCaseSearchTerm),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("commonName")), lowerCaseSearchTerm)
            );
            predicates.add(globalPredicate);
        }

        // --- Column Specific Filtering ---
        filterParams.forEach((key, value) -> {
            if (!key.equals("globalSearch") && !key.equals("page") && !key.equals("size") && !key.startsWith("sort") && !key.equals("isDeleted")) { // Optionally ignore 'isDeleted' from params if always handled
                if (StringUtils.hasText(value)) {
                    if (isTextField(key)) {
                        predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get(key)), "%" + value.toLowerCase() + "%"));
                    } else if (isDateField(key)) {
                        try {
                            LocalDate filterDate = LocalDate.parse(value); // date format YYYY-MM-DD
                            predicates.add(criteriaBuilder.equal(root.get(key), filterDate));
                        } catch (DateTimeParseException e) {
                            System.err.println("Invalid date format for filter '" + key + "': " + value);
                            // Optionally, you might want to throw an exception or handle this error more gracefully
                        }
                    }
                    // else if (isNumericField(key)) { /* Add logic for numeric fields */ }
                }
            }
        });

        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    }

    private boolean isTextField(String fieldName) {
        return fieldName.equals("idDemand") || fieldName.equals("demandeName") ||
                fieldName.equals("model") || fieldName.equals("type") ||
                fieldName.equals("organizationalUnit") || fieldName.equals("commonName");
    }

    private boolean isDateField(String fieldName) {
        return fieldName.equals("creationDate") || fieldName.equals("expirationDate");
    }

    // private boolean isNumericField(String fieldName) { /* Implement if needed */ return false; }
}