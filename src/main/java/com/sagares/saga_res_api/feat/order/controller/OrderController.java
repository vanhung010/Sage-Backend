package com.sagares.saga_res_api.feat.order.controller;

import com.sagares.saga_res_api.common.dto.PageResponse;
import com.sagares.saga_res_api.feat.order.dto.StaffOrderResponse;
import com.sagares.saga_res_api.feat.order.service.OrderService;
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

    @GetMapping("/pending")
    public PageResponse<StaffOrderResponse> getPendingOrders(
            @RequestParam(name = "cursor", required = false) @Size(max = 96) String cursor,
            @RequestParam(name = "size", defaultValue = "20") @Min(1) @Max(100) int size) {
        return orderService.getPendingOrders(cursor, size);
    }
}
