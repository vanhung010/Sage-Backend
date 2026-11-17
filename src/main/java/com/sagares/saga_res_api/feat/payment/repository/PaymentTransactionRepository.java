package com.sagares.saga_res_api.feat.payment.repository;

import com.sagares.saga_res_api.feat.payment.dto.StaffOrderPayment;
import com.sagares.saga_res_api.feat.payment.entity.PaymentMethod;
import com.sagares.saga_res_api.feat.payment.entity.PaymentTransaction;
import com.sagares.saga_res_api.feat.payment.entity.TransactionStatus;
import com.sagares.saga_res_api.feat.payment.entity.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
@Repository
public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {

    @Query("""
            SELECT p.order.id AS orderId, p.method AS method, p.status AS status
            FROM PaymentTransaction p
            WHERE p.order.id IN :orderIds AND p.type = :type
            ORDER BY CASE WHEN p.status = :successful THEN 0 ELSE 1 END,
                     p.createdAt DESC, p.id DESC
            """)
    List<StaffOrderPayment> findOrderPayments(
            @Param("orderIds") Collection<Long> orderIds,
            @Param("type") TransactionType type,
            @Param("successful") TransactionStatus successful);


}