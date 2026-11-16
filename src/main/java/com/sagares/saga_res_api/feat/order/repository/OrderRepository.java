package com.sagares.saga_res_api.feat.order.repository;

import com.sagares.saga_res_api.feat.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Long, Order> {

}
