package com.jewellery.cartservice.repository;

import com.jewellery.cartservice.entity.ShoppingCart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CartRepository extends JpaRepository<ShoppingCart, UUID> {
    Optional<ShoppingCart> findByUserId(UUID userId);
}
