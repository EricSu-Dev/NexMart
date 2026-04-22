package com.nex.nexmart.service.intf;

import com.nex.nexmart.common.PageResult;
import com.nex.nexmart.model.dto.PromotionDTO;
import com.nex.nexmart.model.entity.Promotion;
import com.baomidou.mybatisplus.extension.service.IService;
import com.nex.nexmart.model.entity.product.Product;
import com.nex.nexmart.model.vo.PromotionVO;
import com.nex.nexmart.model.vo.product.BrowseHistoryVO;
import com.nex.nexmart.model.vo.product.FavoriteProductVO;

import java.math.BigDecimal;
import java.util.List;

/**
* @author Eric
* @description 针对表【promotion(限时优惠活动表)】的数据库操作Service
* @createDate 2026-04-06 19:43:47
*/
public interface PromotionService extends IService<Promotion> {
	PageResult<PromotionVO> pagePromotions(long current, long size, Integer status, Integer scope, Integer stage);
	void createPromotion(PromotionDTO dto);
	void updatePromotion(Long id, PromotionDTO dto);
	void deletePromotion(Long id);
	void updateStatus(Long id, Integer status);

	// 查商品当前生效的活动
	Promotion getActivePromotion(Long productId, Long categoryId, BigDecimal price);

	List<Promotion> getActivePromotionList(List<Long> productIds, List<Long> categoryIds);

	//查询当前商品的最佳优惠活动
	Promotion findBestPromotion(Product product, List<Promotion> activePromotions);
	Promotion findBestPromotion(FavoriteProductVO favoriteProductVO, List<Promotion> activePromotions);
	Promotion findBestPromotion(BrowseHistoryVO browseHistoryVO, List<Promotion> activePromotions);

	// 计算折后价
	BigDecimal calcDiscountedPrice(BigDecimal originalPrice, Promotion promotion);

}