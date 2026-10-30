package com.sagares.saga_res_api.feat.order.repository;

import com.sagares.saga_res_api.feat.order.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

}
