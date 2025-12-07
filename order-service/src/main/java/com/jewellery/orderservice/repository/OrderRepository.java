package com.jewellery.orderservice.repository;

import com.jewellery.orderservice.entity.CustomerOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<CustomerOrder, UUID>, JpaSpecificationExecutor<CustomerOrder> {
    Optional<CustomerOrder> findByOrderNumber(String orderNumber);

    List<CustomerOrder> findByUserId(UUID userId);
}
