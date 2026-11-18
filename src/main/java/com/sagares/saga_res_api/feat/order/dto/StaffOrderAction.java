package com.sagares.saga_res_api.feat.order.dto;

public enum StaffOrderAction {
    RECONCILE_PAYMENT,     // Mở màn hình đối soát giao dịch VNPAY.
    APPROVE_ORDER,         // Duyệt đơn VNPAY đã nhận tiền.
    REJECT_ORDER,          // Từ chối đơn và ghi lý do.
    START_PREPARING,       // Bắt đầu chuẩn bị món.
    START_DELIVERY,        // Bắt đầu giao đơn.
    CONFIRM_COD_COLLECTION,// Mở bước xác nhận đã giao món và thu đủ tiền COD.
    COMPLETE_DELIVERY,     // Xác nhận giao xong khi tiền đã được ghi nhận.
    CANCEL_ORDER,          // Hủy đơn đang chuẩn bị hoặc đang giao, kèm lý do.
    REFRESH_REFUND_STATUS  // Tải lại trạng thái hoàn tiền.
}