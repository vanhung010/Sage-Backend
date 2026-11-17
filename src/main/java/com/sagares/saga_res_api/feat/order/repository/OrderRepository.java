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
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    //EntityGraph tải entity user cùng với đơn, không cần viết sql join fetch
    @EntityGraph(attributePaths = "user")
    List<Order> findByStatusAndDeletedFalseOrderByCreatedAtAscIdAsc(Status status, Pageable pageable);

    //Tìm cho lần lấy danh sách tiếp theo
    @EntityGraph(attributePaths = "user")
    @Query("""
            SELECT o FROM Order o
            WHERE o.status = :status AND o.deleted = false
              AND (o.createdAt > :createdAt OR (o.createdAt = :createdAt AND o.id > :id))
            ORDER BY o.createdAt ASC, o.id ASC
            """)
    List<Order> findAfter(
            @Param("status") Status status,
            @Param("createdAt") LocalDateTime createdAt,
            @Param("id") long id,
            Pageable pageable);

}
