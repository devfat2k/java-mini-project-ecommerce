package com.devfat.mini_ecommerce.entity.order;

import com.devfat.mini_ecommerce.entity.orderItem.OrderItemEntity;
import com.devfat.mini_ecommerce.entity.user.UserEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@DynamicUpdate
@DynamicInsert
@Entity
@Builder
@Table(name = "orders")
public class OrderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Enumerated(EnumType.STRING)
    @Column(length = 10, nullable = false)
    private OrderStatus status;

    @Column
    private BigDecimal total_amount;

    @Column(length = 1000)
    private String note;

    // ← 1-N: 1 Order có nhiều OrderItem
    // cascade ALL: save/delete Order sẽ tự save/delete các Item, orphanRemoval: xoá item khỏi list = xoá khỏi DB
    //
    @OneToMany(mappedBy = "order",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    @Builder.Default
    private List<OrderItemEntity> items = new ArrayList<>();


    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum OrderStatus { DONE, PENDING, SHIPPED, CONFIRMED }
}
