package com.nex.nexmart.service.impl.order;

import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.domain.AlipayTradeRefundModel;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.domain.AlipayTradePagePayModel;
import com.alipay.api.request.AlipayTradeRefundRequest;
import com.alipay.api.response.AlipayTradeRefundResponse;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nex.nexmart.common.constant.OrderStatusConstants;
import com.nex.nexmart.common.constant.ReturnOrderStatusConstant;
import com.nex.nexmart.config.properties.AlipayProperties;
import com.nex.nexmart.exception.BusinessException;
import com.nex.nexmart.mapper.OrderMapper;
import com.nex.nexmart.mapper.base.PaymentMapper;
import com.nex.nexmart.mapper.base.ReturnOrderMapper;
import com.nex.nexmart.model.entity.coupon.CouponUser;
import com.nex.nexmart.model.entity.product.Product;
import com.nex.nexmart.model.entity.order.Order;
import com.nex.nexmart.model.entity.order.OrderItem;
import com.nex.nexmart.model.entity.order.Payment;
import com.nex.nexmart.model.entity.order.ReturnOrder;
import com.nex.nexmart.service.intf.coupon.CouponUserService;
import com.nex.nexmart.service.intf.order.OrderItemService;
import com.nex.nexmart.service.intf.order.PaymentService;
import com.nex.nexmart.service.intf.product.ProductService;
import com.nex.nexmart.websocket.WebSocketSessionManager;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl extends ServiceImpl<PaymentMapper, Payment> implements PaymentService {

    private final AlipayProperties alipayProperties;
	private final AlipayClient alipayClient;
    private final OrderMapper orderMapper;
	private final OrderItemService orderItemService;
	private final ProductService productService;
	private final ReturnOrderMapper returnOrderMapper;
	private final WebSocketSessionManager sessionManager;
	private final CouponUserService couponUserService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String pay(Long orderId, Long userId) {
        // 1. 查订单，校验归属和状态
        Order order = orderMapper.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BusinessException("订单不存在");
        }
        if (order.getStatus() != 1) {
            throw new BusinessException("订单状态异常，无法支付");
        }
        if (order.getPayStatus() != null && order.getPayStatus() == 1) {
            throw new BusinessException("订单已支付");
        }

        // 2. 创建或查找支付记录（防止重复创建）
        Payment payment = lambdaQuery()
                .eq(Payment::getOrderId, orderId)
                .one();

        if (payment == null) {
            payment = new Payment();
            payment.setOrderId(order.getId());
            payment.setOrderNo(order.getOrderNo());
            payment.setAmount(order.getFinalAmount());
            payment.setPayType(1);
            payment.setStatus(0);
            save(payment);
	        // 用完整的 outTradeNo 更新 order_no
	        //为什么加后缀? 因为支付宝不允许重复订单号，用户可能多次发起支付,所以加上后缀
	        String outTradeNo = order.getOrderNo() + "_" + payment.getId();
	        lambdaUpdate()
			        .eq(Payment::getId, payment.getId())
			        .set(Payment::getOrderNo, outTradeNo)
			        .update();
        }

        // 3. 构建电脑网站支付请求模型（官方推荐方式，更稳健）
        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
        request.setNotifyUrl(alipayProperties.getNotifyUrl());
        request.setReturnUrl(alipayProperties.getReturnUrl());

        // 业务参数准备
	    String outTradeNo = payment.getOrderNo();
        String subject = "NexMart Order - " + order.getOrderNo();
        String totalAmount = order.getFinalAmount().toPlainString();

        AlipayTradePagePayModel model = new AlipayTradePagePayModel();
        model.setOutTradeNo(outTradeNo);
        model.setTotalAmount(totalAmount);
        model.setSubject(subject);
        model.setProductCode("FAST_INSTANT_TRADE_PAY");
        model.setQrPayMode("2");           // 订单码-跳转模式
        model.setIntegrationType("PCWEB"); // PC端访问

        request.setBizModel(model);

        log.info("拉起支付宝支付: outTradeNo={}, totalAmount={}, subject={}", outTradeNo, totalAmount, subject);

        // 4. 生成表单 HTML 并返回
        try {
            String form = alipayClient.pageExecute(request).getBody();
            log.info("支付宝支付表单生成成功，orderNo={}", order.getOrderNo());
            return form;
        } catch (AlipayApiException e) {
            log.error("支付宝支付表单生成失败", e);
            throw new BusinessException("发起支付失败，请稍后重试");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String handleNotify(HttpServletRequest request) {
        // 1. 收集支付宝回调的所有参数
	    //把 HTTP 请求中的所有参数，从 Map<String, String[]> 格式转换成 Map<String, String> 格式，方便后续支付宝签名验证使用。
        Map<String, String> params = new HashMap<>();
        Map<String, String[]> requestParams = request.getParameterMap();
		//keySet() 是 Map 的方法，用于获取 Map 中所有键（Key）的集合
	    //keySet()提取所有键 返回Set集合
	    //values()提取所有值,返回Collection集合
	    //entrySet()提取所有键值对返回Set集合

	    //// keySet() 只是用来"知道有哪些键"，负责驱动循环
        for (String name : requestParams.keySet()) {
			//把String[] 转换成 String,用 , 连接
	        params.put(name, String.join(",", requestParams.get(name)));
        }

        log.info("收到支付宝异步通知: {}", params);

        // 2. 验签（非常重要，防止伪造回调）
        try {
            boolean signVerified = AlipaySignature.rsaCheckV1(
                    params,
                    alipayProperties.getAlipayPublicKey(),
                    alipayProperties.getCharset(),
                    alipayProperties.getSignType()
            );
            if (!signVerified) {
                log.warn("支付宝回调验签失败");
                return "failure";
            }
        } catch (AlipayApiException e) {
            log.error("支付宝验签异常", e);
            return "failure";
        }

        // 3. 取关键参数
	    String tradeStatus = params.get("trade_status");
	    String outTradeNo  = params.get("out_trade_no");
	    String tradeNo     = params.get("trade_no");

	    log.info("支付宝通知 outTradeNo={} tradeNo={} tradeStatus={}", outTradeNo, tradeNo, tradeStatus);

		// 4. 只处理支付成功的通知,支付宝会发多种通知（等待付款、退款等），这里只处理支付成功的情况，其他的直接跳过。
	    if (!"TRADE_SUCCESS".equals(tradeStatus) && !"TRADE_FINISHED".equals(tradeStatus)) {
		    return "success";
	    }

		// 5. 从 outTradeNo 提取原始 orderNo（兼容带后缀和不带后缀）
	    String orderNo = outTradeNo.contains("_")
			            //提取0到最后一个_之间的字符
			            ? outTradeNo.substring(0, outTradeNo.lastIndexOf("_"))
			            : outTradeNo;

		// 先查订单
	    Order order = orderMapper.selectOne(new QueryWrapper<Order>().eq("order_no", orderNo));

	    if (order == null) {
		    log.warn("找不到对应的订单 orderNo={}", orderNo);
		    return "failure";
	    }

		// 再用 orderId 查支付记录
	    Payment payment = lambdaQuery()
			    .eq(Payment::getOrderId, order.getId())
			    .one();

	    if (payment == null) {
		    log.warn("找不到对应的支付记录 orderId={}", order.getId());
		    return "failure";
	    }

		// 6. 幂等：支付宝可能会多次发送同一个通知,已经处理过就直接返回成功
	    if (payment.getStatus() == 1) {
		    log.info("订单已支付，忽略重复通知 orderNo={}", orderNo);
		    return "success";
	    }

        // 7. 更新支付记录
        lambdaUpdate()
                .eq(Payment::getId, payment.getId())
                .set(Payment::getStatus, 1)
                .set(Payment::getPayNo, tradeNo)
                .set(Payment::getPayTime, LocalDateTime.now())
                .update();

        // 8. 更新订单状态：待付款(1) → 待发货(2)，同时标记已支付
		orderMapper.update(new LambdaUpdateWrapper<Order>()
				.eq(Order::getOrderNo, outTradeNo)
				.set(Order::getStatus, OrderStatusConstants.PENDING_DELIVERY)
				.set(Order::getPayStatus, 1));

        log.info("订单支付成功处理完成 orderNo={}", outTradeNo);
	    // 9.支付回调成功时,通知商家
	    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
	    String message = JSON.toJSONString(Map.of(
			    "type", "NEW_ORDER",
			    "orderId", order.getId(),
			    "amount", order.getFinalAmount(),
			    "time", LocalDateTime.now().format(formatter)
	    ));
	    sessionManager.broadcast(message);
        return "success";
    }

	@Override
	public void refund(Long returnId) {
		ReturnOrder one = returnOrderMapper.selectById(returnId);
		if (one == null) {
			throw new BusinessException("退货订单不存在");
		}
		if (one.getStatus() != ReturnOrderStatusConstant.REFUND_PROCESSING) {
			throw new BusinessException("退货订单状态错误");
		}

		// 查询原订单获取订单号
		Order order = orderMapper.selectOne(new LambdaQueryWrapper<Order>().eq(Order::getId, one.getOrderId()));

		try {
			AlipayTradeRefundRequest request = new AlipayTradeRefundRequest();
			AlipayTradeRefundModel model = new AlipayTradeRefundModel();

			model.setOutTradeNo(order.getOrderNo());                           // 原支付订单号
			model.setRefundAmount(one.getActualRefundAmount().toString());     // 实际退款金额
			model.setRefundReason(one.getReason());                            // 退款原因
			model.setOutRequestNo(one.getId().toString());                     // 退款请求号，唯一

			request.setBizModel(model);
			AlipayTradeRefundResponse response = alipayClient.execute(request);

			if (response.isSuccess()) {
				// 退款成功，更新状态为已退款
				one.setStatus(ReturnOrderStatusConstant.REFUNDED);
				returnOrderMapper.updateById(one);
				//更新支付记录
				lambdaUpdate().eq(Payment::getOrderNo, order.getOrderNo()).set(Payment::getStatus, 2).update();
				// 回滚销量
				OrderItem orderItem = orderItemService.getById(one.getOrderItemId());
				productService.lambdaUpdate()
						.eq(Product::getId, orderItem.getProductId())
						.setSql("sales = sales - " + orderItem.getQuantity())
						.update();

				// 回滚商品券与订单券
				couponUserService.lambdaUpdate()
								.eq(CouponUser::getOrderId, order.getId())
								.eq(CouponUser::getStatus, 1)
								.set(CouponUser::getStatus, 0)
								.set(CouponUser::getUsedAt, null)
								.set(CouponUser::getOrderId, null)
								.update();
			} else {
				// 退款失败
				throw new BusinessException("退款失败：" + response.getSubMsg());
			}
		} catch (AlipayApiException e) {
			throw new BusinessException("支付宝退款异常：" + e.getMessage());
		}
	}
}
