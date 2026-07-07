package com.nex.nexmart.service.impl.order;

import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import com.nex.nexmart.config.RabbitMQConfig;
import com.nex.nexmart.model.dto.order.OrderCreateDTO;
import com.nex.nexmart.model.dto.order.OrderPreviewDTO;
import com.nex.nexmart.model.entity.CartItem;
import com.nex.nexmart.model.entity.Promotion;
import com.nex.nexmart.model.entity.order.Order;
import com.nex.nexmart.model.entity.product.Product;
import com.nex.nexmart.model.vo.order.OrderPreviewVO;
import com.nex.nexmart.service.intf.CartItemService;
import com.nex.nexmart.service.intf.PromotionService;
import com.nex.nexmart.service.intf.coupon.CouponService;
import com.nex.nexmart.service.intf.coupon.CouponUserService;
import com.nex.nexmart.service.intf.order.OrderItemService;
import com.nex.nexmart.service.intf.order.ReturnOrderService;
import com.nex.nexmart.service.intf.product.ProductService;
import com.nex.nexmart.service.intf.product.ProductSpecService;
import com.nex.nexmart.service.intf.review.ReviewService;
import com.nex.nexmart.service.intf.seckill.SeckillItemService;
import com.nex.nexmart.websocket.WebSocketSessionManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OrderServiceImplTest {

	private ThreadPoolExecutor executor;

	@AfterEach
	void tearDown() {
		if (executor != null) {
			executor.shutdownNow();
		}
	}

	@Test
	void createOrderDeductsStockPersistsOrderAndSchedulesTimeoutMessage() {
		OrderItemService orderItemService = mock(OrderItemService.class);
		CartItemService cartItemService = mock(CartItemService.class);
		ProductService productService = mock(ProductService.class);
		ReviewService reviewService = mock(ReviewService.class);
		ProductSpecService productSpecService = mock(ProductSpecService.class);
		ReturnOrderService returnOrderService = mock(ReturnOrderService.class);
		WebSocketSessionManager sessionManager = mock(WebSocketSessionManager.class);
		PromotionService promotionService = mock(PromotionService.class);
		CouponUserService couponUserService = mock(CouponUserService.class);
		CouponService couponService = mock(CouponService.class);
		SeckillItemService seckillItemService = mock(SeckillItemService.class);
		RabbitTemplate rabbitTemplate = mock(RabbitTemplate.class);
		RabbitMQConfig rabbitMQConfig = mock(RabbitMQConfig.class);
		@SuppressWarnings("unchecked")
		RedisTemplate<String, String> redisTemplate = mock(RedisTemplate.class);
		OrderServiceImpl orderService = spy(new OrderServiceImpl(
				orderItemService,
				cartItemService,
				productService,
				reviewService,
				productSpecService,
				returnOrderService,
				sessionManager,
				promotionService,
				couponUserService,
				couponService,
				seckillItemService,
				rabbitTemplate,
				rabbitMQConfig,
				redisTemplate));
		executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(1);
		ReflectionTestUtils.setField(orderService, "executor", executor);

		Long userId = 7L;
		CartItem cartItem = new CartItem();
		cartItem.setId(100L);
		cartItem.setUserId(userId);
		cartItem.setProductId(200L);
		cartItem.setQuantity(2);
		Product product = new Product();
		product.setId(200L);
		product.setName("Keyboard");
		product.setCategoryId(300L);
		product.setPrice(new BigDecimal("99.00"));
		product.setStock(10);
		product.setStatus(1);
		product.setCoverUrl("cover.jpg");
		OrderCreateDTO dto = new OrderCreateDTO();
		dto.setCartItemIds(List.of(cartItem.getId()));
		dto.setAddressId(1L);
		dto.setReceiverName("Eric");
		dto.setReceiverPhone("13800138000");
		dto.setAddress("Shanghai");
		OrderPreviewVO preview = new OrderPreviewVO();
		preview.setOriginalAmount(new BigDecimal("198.00"));
		preview.setPromotionTotalDiscount(BigDecimal.ZERO);
		preview.setProductCouponTotalDiscount(BigDecimal.ZERO);
		preview.setProductCouponDiscountMap(java.util.Map.of());
		preview.setFinalAmount(new BigDecimal("198.00"));

		@SuppressWarnings("unchecked")
		LambdaQueryChainWrapper<Product> productQuery = mock(LambdaQueryChainWrapper.class);
		@SuppressWarnings("unchecked")
		LambdaUpdateChainWrapper<Product> productUpdate = mock(LambdaUpdateChainWrapper.class);
		when(cartItemService.listByIds(List.of(cartItem.getId()))).thenReturn(List.of(cartItem));
		when(productService.lambdaQuery()).thenReturn(productQuery);
		when(productQuery.in(org.mockito.Mockito.<SFunction<Product, ?>>any(), anyCollection())).thenReturn(productQuery);
		when(productQuery.list()).thenReturn(List.of(product));
		when(promotionService.getActivePromotionList(List.of(product.getId()), List.of(product.getCategoryId())))
				.thenReturn(List.<Promotion>of());
		when(promotionService.findBestPromotion(eq(product), anyList())).thenReturn(null);
		when(productService.lambdaUpdate()).thenReturn(productUpdate);
		when(productUpdate.eq(org.mockito.Mockito.<SFunction<Product, ?>>any(), any())).thenReturn(productUpdate);
		when(productUpdate.ge(org.mockito.Mockito.<SFunction<Product, ?>>any(), any())).thenReturn(productUpdate);
		when(productUpdate.setSql(any())).thenReturn(productUpdate);
		when(productUpdate.update()).thenReturn(true);
		when(orderItemService.saveBatch(anyCollection())).thenReturn(true);
		when(cartItemService.removeByIds(List.of(cartItem.getId()))).thenReturn(true);
		doReturn(preview).when(orderService).preview(eq(userId), any(OrderPreviewDTO.class));
		doAnswer(invocation -> {
			Order order = invocation.getArgument(0);
			order.setId(900L);
			return true;
		}).when(orderService).save(any(Order.class));

		String orderNo = orderService.createOrder(dto, userId);

		assertThat(orderNo).isNotBlank();
		verify(productUpdate).update();
		verify(orderItemService).saveBatch(anyCollection());
		verify(cartItemService).removeByIds(List.of(cartItem.getId()));
		verify(rabbitMQConfig).sendOrderTimeoutMessage(900L);
	}
}
