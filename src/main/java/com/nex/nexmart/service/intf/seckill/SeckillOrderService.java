package com.nex.nexmart.service.intf.seckill;

import com.nex.nexmart.model.dto.seckill.SeckillProductOrderDTO;

public interface SeckillOrderService {
	void createCouponOrder(Long userId, Long seckillItemId);
	void createProductOrder(Long userId, SeckillProductOrderDTO dto);
}
