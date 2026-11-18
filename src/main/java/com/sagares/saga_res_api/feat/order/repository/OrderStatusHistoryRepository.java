package com.sagares.saga_res_api.feat.order.repository;

import com.sagares.saga_res_api.feat.order.entity.OrderStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderStatusHistoryRepository
        extends JpaRepository<OrderStatusHistory, Long> {
}