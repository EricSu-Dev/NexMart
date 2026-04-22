package com.nex.nexmart.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nex.nexmart.model.entity.seckill.SeckillItem;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nex.nexmart.model.vo.seckill.SeckillCouponItemVO;
import com.nex.nexmart.model.vo.seckill.SeckillProductItemVO;

import java.util.List;

/**
 * @author Eric
 * 针对表【seckill_item(秒杀商品表)】的数据库操作Mapper
 * 2026-04-13 16:10:37
 * com.nex.nexmart.model.entity.seckill.SeckillItem
 */
public interface SeckillItemMapper extends BaseMapper<SeckillItem> {
	IPage<SeckillProductItemVO> productList(Page<SeckillProductItemVO> pageParam, Boolean onlyUnbound, Long activityId);
	IPage<SeckillCouponItemVO> couponList(Page<SeckillCouponItemVO> pageParam, Boolean onlyUnbound, Long activityId);

	List<SeckillProductItemVO> productListByActivity(Long activityId, Long userId);
	List<SeckillCouponItemVO> couponListByActivity(Long activityId, Long userId);
}




