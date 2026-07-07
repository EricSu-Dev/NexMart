package com.nex.nexmart.service.impl.seckill;

import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.nex.nexmart.common.constant.RabbitMQConstants;
import com.nex.nexmart.config.RabbitMQConfig;
import com.nex.nexmart.mapper.CouponUserMapper;
import com.nex.nexmart.mapper.base.CouponMapper;
import com.nex.nexmart.model.dto.seckill.SeckillProductOrderDTO;
import com.nex.nexmart.model.entity.Address;
import com.nex.nexmart.model.entity.order.Order;
import com.nex.nexmart.model.entity.product.Product;
import com.nex.nexmart.model.entity.seckill.SeckillActivity;
import com.nex.nexmart.model.entity.seckill.SeckillItem;
import com.nex.nexmart.rabbit.mq.message.SeckillProductOrderMessage;
import com.nex.nexmart.service.intf.AddressService;
import com.nex.nexmart.service.intf.order.OrderItemService;
import com.nex.nexmart.service.intf.order.OrderService;
import com.nex.nexmart.service.intf.product.ProductService;
import com.nex.nexmart.service.intf.product.ProductSpecService;
import com.nex.nexmart.service.intf.seckill.SeckillActivityService;
import com.nex.nexmart.service.intf.seckill.SeckillItemService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SeckillOrderServiceImplTest {

	@Test
	void createProductOrderReservesRedisStockAndPublishesAsyncMessage() {
		SeckillItemService seckillItemService = mock(SeckillItemService.class);
		SeckillActivityService seckillActivityService = mock(SeckillActivityService.class);
		ProductService productService = mock(ProductService.class);
		ProductSpecService productSpecService = mock(ProductSpecService.class);
		OrderService orderService = mock(OrderService.class);
		OrderItemService orderItemService = mock(OrderItemService.class);
		AddressService addressService = mock(AddressService.class);
		CouponMapper couponMapper = mock(CouponMapper.class);
		RabbitMQConfig rabbitMQConfig = mock(RabbitMQConfig.class);
		CouponUserMapper couponUserMapper = mock(CouponUserMapper.class);
		@SuppressWarnings("unchecked")
		RedisTemplate<String, String> redisTemplate = mock(RedisTemplate.class);
		SeckillOrderServiceImpl seckillOrderService = new SeckillOrderServiceImpl(
				seckillItemService,
				seckillActivityService,
				productService,
				productSpecService,
				orderService,
				orderItemService,
				addressService,
				couponMapper,
				rabbitMQConfig,
				couponUserMapper,
				redisTemplate);

		Long userId = 7L;
		SeckillProductOrderDTO dto = new SeckillProductOrderDTO();
		dto.setSeckillItemId(88L);
		dto.setAddressId(99L);
		SeckillItem item = new SeckillItem();
		item.setId(dto.getSeckillItemId());
		item.setActivityId(1L);
		item.setProductId(2L);
		item.setStatus(1);
		item.setPerLimit(1);
		item.setSeckillStock(5);
		item.setSeckillPrice(new BigDecimal("49.00"));
		SeckillActivity activity = new SeckillActivity();
		activity.setId(1L);
		activity.setStatus(1);
		activity.setStartTime(LocalDateTime.now().minusMinutes(1));
		activity.setEndTime(LocalDateTime.now().plusMinutes(30));
		Product product = new Product();
		product.setId(2L);
		product.setHasSpec(0);
		product.setPrice(new BigDecimal("99.00"));
		Address address = new Address();
		address.setId(dto.getAddressId());
		address.setUserId(userId);

		@SuppressWarnings("unchecked")
		LambdaQueryChainWrapper<Address> addressQuery = mock(LambdaQueryChainWrapper.class);
		when(seckillItemService.getById(dto.getSeckillItemId())).thenReturn(item);
		when(seckillActivityService.getById(item.getActivityId())).thenReturn(activity);
		when(productService.getById(item.getProductId())).thenReturn(product);
		when(addressService.lambdaQuery()).thenReturn(addressQuery);
		when(addressQuery.eq(org.mockito.Mockito.<SFunction<Address, ?>>any(), any())).thenReturn(addressQuery);
		when(addressQuery.one()).thenReturn(address);
		when(redisTemplate.hasKey(anyString())).thenReturn(true);
		when(redisTemplate.execute(any(DefaultRedisScript.class), anyList(), anyString(), anyString())).thenReturn(0L);

		seckillOrderService.createProductOrder(userId, dto);

		ArgumentCaptor<Object> messageCaptor = ArgumentCaptor.forClass(Object.class);
		verify(rabbitMQConfig).sendSeckillMessage(
				eq(RabbitMQConstants.SECKILL_ORDER_EXCHANGE),
				eq(RabbitMQConstants.SECKILL_PRODUCT_ORDER_ROUTING_KEY),
				messageCaptor.capture(),
				any(Runnable.class));
		assertThat(messageCaptor.getValue()).isInstanceOf(SeckillProductOrderMessage.class);
		SeckillProductOrderMessage message = (SeckillProductOrderMessage) messageCaptor.getValue();
		assertThat(message.getUserId()).isEqualTo(userId);
		assertThat(message.getSeckillItemId()).isEqualTo(dto.getSeckillItemId());
		assertThat(message.getAddressId()).isEqualTo(dto.getAddressId());
	}
}
