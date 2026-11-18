package com.sagares.saga_res_api.feat.order.controller;

import com.sagares.saga_res_api.common.dto.PageResponse;
import com.sagares.saga_res_api.feat.order.dto.StaffOrderDetailResponse;
import com.sagares.saga_res_api.feat.order.dto.StaffOrderResponse;
import com.sagares.saga_res_api.feat.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    @Operation(
            summary = "Lấy danh sách đơn chờ duyệt",
            description = "Lấy các đơn PENDING đang chờ nhân viên duyệt hoặc từ chối."
    )
    @GetMapping("/pending")
    public PageResponse<StaffOrderResponse> getPendingOrders(
            @RequestParam(name = "cursor", required = false) @Size(max = 96) String cursor,
            @RequestParam(name = "size", defaultValue = "20") @Min(1) @Max(100) int size) {
        return orderService.getPendingOrders(cursor, size);
    }

    @Operation(
            summary = "Lấy danh sách đơn đang xử lý",
            description = "Lấy các đơn đã duyệt (ACCEPTED), đang chuẩn bị món (PREPARING) "
                    + "hoặc đang giao (DELIVERING)."
    )
    @GetMapping("/processing")
    public PageResponse<StaffOrderResponse> getProcessingOrders(
            @RequestParam(name = "cursor", required = false) @Size(max = 96) String cursor,
            @RequestParam(name = "size", defaultValue = "20") @Min(1) @Max(100) int size) {

        return orderService.getProcessingOrders(cursor, size);
    }

    @Operation(
            summary = "Lấy danh sách đơn đã kết thúc",
            description = "Lấy các đơn bị từ chối (DENIED), đã hoàn thành (SUCCESSFUL) "
                    + "hoặc đã hoàn tiền (REFUND)."
    )
    @GetMapping("/finished")
    public PageResponse<StaffOrderResponse> getFinishedOrders(
            @RequestParam(name = "cursor", required = false) @Size(max = 96) String cursor,
            @RequestParam(name = "size", defaultValue = "20") @Min(1) @Max(100) int size) {

        return orderService.getFinishedOrders(cursor, size);
    }

    @Operation(
            summary = "Lấy chi tiết đơn cho nhân viên",
            description = "Trả về món, giao hàng, thanh toán, hoàn tiền và các thao tác có thể thực hiện."
    )
    @GetMapping("/{orderId}")
    public StaffOrderDetailResponse getOrderDetail(
            @PathVariable("orderId") @Positive Long orderId) {
        return orderService.getOrderDetail(orderId);
    }

    @Operation(summary = "Duyệt đơn VNPAY đã xác nhận thanh toán")
    @PatchMapping("/{orderId}/approve")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void approveOrder(@PathVariable("orderId") @Positive Long orderId) {
        orderService.approveOrder(orderId);
    }

}
