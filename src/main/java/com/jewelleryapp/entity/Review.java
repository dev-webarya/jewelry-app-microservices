package com.jewelleryapp.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "reviews")
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Optional link to order to prove they bought it
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "verified_order_id")
    private CustomerOrder verifiedOrder;

    @Column(nullable = false)
    private Integer rating; // 1-5

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String comment;

    @Column(nullable = false)
    private boolean isVerifiedPurchase;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
}