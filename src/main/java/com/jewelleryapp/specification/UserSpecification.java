package com.jewelleryapp.specification;

import com.jewelleryapp.entity.User;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Component
public class UserSpecification {

    public Specification<User> searchByTerm(String term) {
        return (root, query, criteriaBuilder) -> {
            if (term == null || term.trim().isEmpty()) {
                return criteriaBuilder.conjunction();
            }

            String likePattern = "%" + term.toLowerCase() + "%";

            return criteriaBuilder.or(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("firstName")), likePattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("lastName")), likePattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("email")), likePattern), // Changed from username
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("phoneNumber")), likePattern) // Added phone number
            );
        };
    }
}