package com.sagares.saga_res_api.feat.payment.dto;

import com.sagares.saga_res_api.feat.payment.entity.PaymentMethod;
import com.sagares.saga_res_api.feat.payment.entity.TransactionStatus;

public interface StaffOrderPayment {
    Long getOrderId();
    PaymentMethod getMethod();
    TransactionStatus getStatus();
}
