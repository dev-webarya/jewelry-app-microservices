package com.jewelleryapp.specification;

import com.jewelleryapp.entity.CustomerOrder;
import com.jewelleryapp.enums.OrderStatus;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class OrderSpecification {

    public static Specification<CustomerOrder> hasUserId(UUID userId) {
        return (root, query, cb) -> {
            if (userId == null) return cb.conjunction();
            return cb.equal(root.get("user").get("id"), userId);
        };
    }

    public static Specification<CustomerOrder> hasFulfillmentStoreId(UUID storeId) {
        return (root, query, cb) -> {
            if (storeId == null) return cb.conjunction();
            return cb.equal(root.get("fulfillmentStore").get("id"), storeId);
        };
    }

    public static Specification<CustomerOrder> hasStatus(OrderStatus status) {
        return (root, query, cb) -> {
            if (status == null) return cb.conjunction();
            return cb.equal(root.get("status"), status);
        };
    }

    public static Specification<CustomerOrder> hasOrderNumber(String orderNumber) {
        return (root, query, cb) -> {
            if (orderNumber == null || orderNumber.isEmpty()) return cb.conjunction();
            return cb.like(cb.lower(root.get("orderNumber")), "%" + orderNumber.toLowerCase() + "%");
        };
    }

    public static Specification<CustomerOrder> createdAfter(LocalDateTime date) {
        return (root, query, cb) -> {
            if (date == null) return cb.conjunction();
            return cb.greaterThanOrEqualTo(root.get("createdAt"), date);
        };
    }

    public static Specification<CustomerOrder> createdBefore(LocalDateTime date) {
        return (root, query, cb) -> {
            if (date == null) return cb.conjunction();
            return cb.lessThanOrEqualTo(root.get("createdAt"), date);
        };
    }

    public static Specification<CustomerOrder> priceBetween(BigDecimal min, BigDecimal max) {
        return (root, query, cb) -> {
            if (min == null && max == null) return cb.conjunction();
            if (min != null && max != null) return cb.between(root.get("totalPrice"), min, max);
            if (min != null) return cb.greaterThanOrEqualTo(root.get("totalPrice"), min);
            return cb.lessThanOrEqualTo(root.get("totalPrice"), max);
        };
    }
}