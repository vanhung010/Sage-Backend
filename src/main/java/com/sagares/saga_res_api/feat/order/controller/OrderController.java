package com.sagares.saga_res_api.feat.order.controller;

import com.sagares.saga_res_api.common.dto.PageResponse;
import com.sagares.saga_res_api.feat.order.dto.StaffOrderResponse;
import com.sagares.saga_res_api.feat.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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


}
