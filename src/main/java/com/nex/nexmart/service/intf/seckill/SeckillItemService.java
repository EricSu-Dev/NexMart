package com.nex.nexmart.service.intf.seckill;

import com.nex.nexmart.common.PageResult;
import com.nex.nexmart.model.dto.seckill.AddSeckillItemDTO;
import com.nex.nexmart.model.dto.seckill.BindSeckillItemDTO;
import com.nex.nexmart.model.entity.seckill.SeckillItem;
import com.baomidou.mybatisplus.extension.service.IService;
import com.nex.nexmart.model.vo.seckill.SeckillCouponItemVO;
import com.nex.nexmart.model.vo.seckill.SeckillProductItemVO;

import java.math.BigDecimal;
import java.util.List;

/**
* @author Eric
*  针对表【seckill_item(秒杀商品表)】的数据库操作Service
*  2026-04-13 16:10:37
*/
public interface SeckillItemService extends IService<SeckillItem> {
	void addItem(AddSeckillItemDTO dto);
	void removeItem(Long id);
	void updateItem(Long id, BigDecimal seckillPrice, Integer perLimit);
	void updateStatus(Long id, Integer status);
	PageResult<SeckillProductItemVO> productList(Integer current, Integer size, Boolean onlyUnbound,Long activityId);
	PageResult<SeckillCouponItemVO> couponList(Integer current, Integer size,Boolean onlyUnbound,Long activityId);

	void bindActivity(BindSeckillItemDTO dto);

	void deleteBind(BindSeckillItemDTO dto);

	List<SeckillProductItemVO> productListByActivity(Long activityId, Long userId);

	List<SeckillCouponItemVO> couponListByActivity(Long activityId, Long userId);
}
