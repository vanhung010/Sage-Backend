package com.sagares.saga_res_api.feat.order.entity;

public enum Status {
    PENDING,
    ACCEPTED,
    DENIED,
    DELIVERING,
    SUCCESSFUL,
    PREPARING,
    REFUND
}


//ACCEPTED duyệt đơn, đã nhận tiền ( đối với VnPay)

// PENDING -> ACCEPTED -> PREPARING -> DELIVERING -> SUCCESSFUL (VNPay)
// PENDING -> PREPARING -> DELIVERING -> SUCCESSFUL (Cod)
// PENDING -> DENIED -> REFUND  (Từ chối vì lí do nào đó)
//ACCEPTED -> DENIED (Hủy đơn)