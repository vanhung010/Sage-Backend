package com.sagares.saga_res_api.feat.payment.entity;

public enum TransactionStatus {
    PENDING,    // PaymentMethod COD + PENDING -> Chưa thu tiền
    SUCCESS,    // PaymentMethod VnPay + PENDING -> Chờ xác nhận
    FAILED       // PaymentMethod VnPay + SUCCESS -> Đã nhận tiền
}