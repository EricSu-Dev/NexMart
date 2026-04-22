package com.nex.nexmart.service.intf.order;

import com.baomidou.mybatisplus.extension.service.IService;
import com.nex.nexmart.model.entity.order.Payment;

import jakarta.servlet.http.HttpServletRequest;

public interface PaymentService extends IService<Payment> {

    /**
     * 发起支付宝支付，返回支付表单 HTML
     * 前端直接把这段 HTML 写入页面并提交，会自动跳转支付宝收银台
     *
     * @param orderId 订单ID
     * @param userId  当前用户ID（用于校验订单归属）
     * @return 支付宝 form 表单字符串
     */
    String pay(Long orderId, Long userId);

    /**
     * 支付宝异步回调（notify）
     * 支付宝服务器主动 POST 到你的 notifyUrl，用于更新订单状态
     *
     * @param request HttpServletRequest（取所有参数验签）
     * @return "success" 表示处理成功，支付宝收到后不再重发
     */
    String handleNotify(HttpServletRequest request);

	void refund(Long returnId);
}
