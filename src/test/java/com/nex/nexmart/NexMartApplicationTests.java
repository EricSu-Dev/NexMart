package com.nex.nexmart;

import com.nex.nexmart.service.impl.coupon.CouponUserServiceImpl;
import com.nex.nexmart.service.impl.order.OrderServiceImpl;
import com.nex.nexmart.service.impl.product.SearchServiceImpl;
import com.nex.nexmart.service.impl.seckill.SeckillItemServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
class NexMartApplicationTests {

	@MockitoBean
	private CouponUserServiceImpl couponUserService;

	@MockitoBean
	private SeckillItemServiceImpl seckillItemService;

	@MockitoBean
	private SearchServiceImpl searchService;

	@MockitoBean
	private OrderServiceImpl orderService;

	@Test
	void contextLoads() {
	}
}
