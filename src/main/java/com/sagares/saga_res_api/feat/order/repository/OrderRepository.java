package com.sagares.saga_res_api.feat.order.repository;

import com.sagares.saga_res_api.feat.order.entity.Order;
import com.sagares.saga_res_api.feat.order.entity.Status;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    @EntityGraph(attributePaths = "user")
    List<Order> findByStatusInAndDeletedFalseOrderByCreatedAtAscIdAsc(
            Collection<Status> statuses, Pageable pageable);

    @EntityGraph(attributePaths = "user")
    @Query("""
        SELECT o FROM Order o
        WHERE o.status IN :statuses
          AND o.deleted = false
          AND (
              o.createdAt > :createdAt
              OR (o.createdAt = :createdAt AND o.id > :id)
          )
        ORDER BY o.createdAt ASC, o.id ASC
        """)
    List<Order> findAfter(
            @Param("statuses") Collection<Status> statuses,
            @Param("createdAt") LocalDateTime createdAt,
            @Param("id") long id,
            Pageable pageable);

    //method lấy chi tieets đơn
    @EntityGraph(attributePaths = {"user", "orderItems", "orderItems.menu"})
    Optional<Order> findByIdAndDeletedFalse(Long id);
}
