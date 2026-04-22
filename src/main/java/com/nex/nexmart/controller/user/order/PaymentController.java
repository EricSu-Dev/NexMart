package com.nex.nexmart.controller.user.order;

import com.nex.nexmart.security.SecurityUserDetails;
import com.nex.nexmart.service.intf.order.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@Slf4j
@Tag(name = "用户端-支付接口")
@RestController
@RequestMapping("/api/user/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * 发起支付
     * 返回支付宝 form 表单 HTML，前端写入页面后自动提交跳转收银台
     */
    @Operation(summary = "发起支付宝支付")
    @PostMapping(value = "/pay/{orderId}", produces = "text/html;charset=UTF-8")
    public String pay(@PathVariable Long orderId,
                      @AuthenticationPrincipal SecurityUserDetails userDetails) {
        Long userId = userDetails.getUser().getId();
        log.info("发起支付 orderId={} userId={}", orderId, userId);
        return paymentService.pay(orderId, userId);
    }

    /**
     * 支付宝异步回调（notify）
     * 此接口由支付宝服务器主动调用，不经过用户浏览器
     * 必须配置为 permitAll，否则支付宝回调会被 Security 拦截
     */
    @Operation(summary = "支付宝异步通知回调（供支付宝服务器调用）")
    @PostMapping("/notify")
    public String notify(HttpServletRequest request) {
        log.info("收到支付宝异步通知");
        return paymentService.handleNotify(request);
    }

    /**
     * 支付宝同步跳转（return）
     * 用户在支付宝完成支付后，浏览器跳回此地址
     * 这里直接重定向到前端订单页，让前端自行查询最新状态
     */
    @Operation(summary = "支付完成同步跳转")
    @GetMapping("/return")
    public void payReturn(HttpServletRequest request,
                          HttpServletResponse response) throws IOException {
        log.info("支付宝同步跳转回调");
        // 跳转到前端订单页，前端自己查询订单最新状态
        response.sendRedirect("http://localhost:8085/orders");
    }
}
