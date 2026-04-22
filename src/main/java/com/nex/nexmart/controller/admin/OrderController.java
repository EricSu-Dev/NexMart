package com.nex.nexmart.controller.admin;

import com.nex.nexmart.common.PageResult;
import com.nex.nexmart.common.Result;
import com.nex.nexmart.model.vo.order.OrderVO;
import com.nex.nexmart.service.intf.order.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "管理端-订单接口")
@RestController("adminOrderController")
@RequestMapping("/api/admin/order")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'BOSS')")
@Slf4j
public class OrderController {

    private final OrderService orderService;

    @Operation(summary = "管理员分页查询所有订单")
    @GetMapping("/page")
    public Result<PageResult<OrderVO>> adminPage(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status
    ) {

        log.info("Order admin page current={} size={} status={}", current, size, status);
	    return Result.success(orderService.OrdersPage(current, size, null, status, keyword));
    }

    @Operation(summary = "管理员修改订单状态（如发货）")
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        log.info("Order admin update status id={} status={}", id, status);
        orderService.updateStatus(id, status);
        return Result.success();
    }
}
