package com.nex.nexmart.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nex.nexmart.common.PageResult;
import com.nex.nexmart.common.constant.PromotionStageConstant;
import com.nex.nexmart.exception.BusinessException;
import com.nex.nexmart.mapper.base.CategoryMapper;
import com.nex.nexmart.mapper.base.ProductMapper;
import com.nex.nexmart.model.dto.PromotionDTO;
import com.nex.nexmart.model.entity.Category;
import com.nex.nexmart.model.entity.Promotion;
import com.nex.nexmart.model.entity.product.Product;
import com.nex.nexmart.model.vo.PromotionVO;
import com.nex.nexmart.model.vo.product.BrowseHistoryVO;
import com.nex.nexmart.model.vo.product.FavoriteProductVO;
import com.nex.nexmart.service.intf.PromotionService;
import com.nex.nexmart.mapper.base.PromotionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
* @author Eric
*  针对表【promotion(限时优惠活动表)】的数据库操作Service实现
*  2026-04-06 19:43:47
*/
@Service
@RequiredArgsConstructor
@Slf4j
public class PromotionServiceImpl extends ServiceImpl<PromotionMapper, Promotion> implements PromotionService {

	private final CategoryMapper categoryMapper;
	private final ProductMapper productMapper;
	private final StringRedisTemplate stringRedisTemplate;
	private static final String HOME_MANUAL_SECTION_KEY_PREFIX = "NexMart:home:section:manual:";
	private static final String HOME_AUTO_SECTION_KEY_PREFIX = "NexMart:home:section:auto:";

	@Override
	public PageResult<PromotionVO> pagePromotions(long current, long size, Integer status, Integer scope, Integer stage) {
		LocalDateTime now = LocalDateTime.now();
		Page<Promotion> page = lambdaQuery()
				.eq(status != null, Promotion::getStatus, status)
				.eq(scope != null, Promotion::getScope, scope)
				.gt(PromotionStageConstant.NOT_STARTED .equals(stage), Promotion::getStartTime, now)
				.le(PromotionStageConstant.ONGOING .equals(stage), Promotion::getStartTime, now)
				.ge(PromotionStageConstant.ONGOING .equals(stage), Promotion::getEndTime, now)
				.lt(PromotionStageConstant.ENDED .equals(stage), Promotion::getEndTime, now)
				.orderByDesc(Promotion::getStartTime)
				.page(new Page<>(current, size));

		List<PromotionVO> records = page.getRecords().stream()
				.map(this::toVO)
				.collect(Collectors.toList());

		return PageResult.of(records,page.getTotal(),page.getCurrent(),page.getSize());
	}

	@Override
	public void createPromotion(PromotionDTO dto) {
		validateDTO(dto);
		Promotion promotion = new Promotion();
		BeanUtils.copyProperties(dto, promotion);
		save(promotion);
		//添加活动后及时删除首页模块的缓存
		clearHomeSectionAllCache();
	}

	@Override
	public void updatePromotion(Long id, PromotionDTO dto) {
		Promotion promotion = getAndCheckEditable(id);
		validateDTO(dto);
		BeanUtils.copyProperties(dto, promotion);
		promotion.setId(id);
		updateById(promotion);
		clearHomeSectionAllCache();
	}

	@Override
	public void deletePromotion(Long id) {
		getAndCheckEditable(id);
		removeById(id);
		clearHomeSectionAllCache();
	}

	@Override
	public void updateStatus(Long id, Integer status) {
		Promotion promotion = getById(id);
		if (promotion == null)
		{
			throw new BusinessException("活动不存在");
		}
		lambdaUpdate()
				.eq(Promotion::getId, id)
				.set(Promotion::getStatus, status)
				.update();
		clearHomeSectionAllCache();
	}

	@Override
	public Promotion getActivePromotion(Long productId, Long categoryId, BigDecimal price) {
		List<Promotion> promotions = lambdaQuery()
				.eq(Promotion::getStatus, 1)
				.le(Promotion::getStartTime, LocalDateTime.now())
				.ge(Promotion::getEndTime, LocalDateTime.now())
				.and(w -> w
						.eq(Promotion::getScope, 1)
						.or(i -> i.eq(Promotion::getScope, 2).eq(Promotion::getScopeId, categoryId))
						.or(i -> i.eq(Promotion::getScope, 3).eq(Promotion::getScopeId, productId))
				)
				.list();

		// 算出每个活动的折后价，取最优惠的那个
		return promotions.stream()
				.filter(p -> calcDiscountedPrice(price, p) != null) // 过滤掉未达门槛的
				.min(Comparator.comparing(p -> calcDiscountedPrice(price, p))) // 折后价最低 = 最优惠
				.orElse(null);
	}


	@Override
	public List<Promotion> getActivePromotionList(List<Long> productIds, List<Long> categoryIds) {
		return lambdaQuery()
				.eq(Promotion::getStatus, 1)
				.le(Promotion::getStartTime, LocalDateTime.now())
				.ge(Promotion::getEndTime, LocalDateTime.now())
				.and(w -> w
						.eq(Promotion::getScope, 1)
						.or(i -> i.eq(Promotion::getScope, 2).in(Promotion::getScopeId, categoryIds))
						.or(i -> i.eq(Promotion::getScope, 3).in(Promotion::getScopeId, productIds))
				)
				.list();
	}

	@Override
	public Promotion findBestPromotion(Product product, List<Promotion> activePromotions) {
		return activePromotions.stream()
				.filter(promo ->
						promo.getScope() == 1
								|| (promo.getScope() == 2 && promo.getScopeId().equals(product.getCategoryId()))
								|| (promo.getScope() == 3 && promo.getScopeId().equals(product.getId()))
				)
				.filter(promo -> calcDiscountedPrice(product.getPrice(), promo) != null)
				.min(Comparator.comparing(promo -> calcDiscountedPrice(product.getPrice(), promo)))
				.orElse(null);
	}

	@Override
	public Promotion findBestPromotion(FavoriteProductVO favoriteProductVO, List<Promotion> activePromotions) {
		return activePromotions.stream()
				.filter(promo ->
						promo.getScope() == 1
								|| (promo.getScope() == 2 && promo.getScopeId().equals(favoriteProductVO.getCategoryId()))
								|| (promo.getScope() == 3 && promo.getScopeId().equals(favoriteProductVO.getProductId()))
				)
				.filter(promo -> calcDiscountedPrice(favoriteProductVO.getPrice(), promo) != null)
				.min(Comparator.comparing(promo -> calcDiscountedPrice(favoriteProductVO.getPrice(), promo)))
				.orElse(null);
	}

	@Override
	public Promotion findBestPromotion(BrowseHistoryVO browseHistoryVO, List<Promotion> activePromotions) {
		return activePromotions.stream()
				.filter(promo ->
						promo.getScope() == 1
								|| (promo.getScope() == 2 && promo.getScopeId().equals(browseHistoryVO.getCategoryId()))
								|| (promo.getScope() == 3 && promo.getScopeId().equals(browseHistoryVO.getProductId()))
				)
				.filter(promo -> calcDiscountedPrice(browseHistoryVO.getPrice(), promo) != null)
				.min(Comparator.comparing(promo -> calcDiscountedPrice(browseHistoryVO.getPrice(), promo)))
				.orElse(null);
	}

	@Override
	public BigDecimal calcDiscountedPrice(BigDecimal originalPrice, Promotion promotion) {
		if (promotion == null) return null;
		//// 满减：检查是否达到门槛
		if (originalPrice.compareTo(promotion.getMinAmount()) < 0) {
			return null;
		}
		if (promotion.getType() == 1) {
			return originalPrice.subtract(promotion.getDiscountAmount())
					.max(BigDecimal.ZERO);
		} else {
			// 折扣：乘折扣率
			return originalPrice.multiply(promotion.getDiscountRate())
					.setScale(2, RoundingMode.HALF_UP);
		}
	}



	// ---- 私有方法 ----

	/** 校验活动未开始，才允许修改/删除 */
	private Promotion getAndCheckEditable(Long id) {
		Promotion promotion = getById(id);
		if (promotion == null) throw new BusinessException("活动不存在");
		if (!LocalDateTime.now().isBefore(promotion.getStartTime())&&promotion.getStatus()==1) {
			throw new BusinessException("活动已开始，不建议修改或删除,如果执意要修改请先禁用活动");
		}
		return promotion;
	}

	/** 校验折扣字段与类型匹配 */
	private void validateDTO(PromotionDTO dto) {
		if (dto.getType() == 1 && dto.getDiscountAmount() == null) {
			throw new BusinessException("满减券必须填写满减金额");
		}
		if (dto.getType() == 2 && dto.getDiscountRate() == null) {
			throw new BusinessException("折扣券必须填写折扣率");
		}
		if (dto.getScope() != 1 && dto.getScopeId() == null) {
			throw new BusinessException("指定分类或商品时必须填写对应ID");
		}
		if (!dto.getStartTime().isBefore(dto.getEndTime())) {
			throw new BusinessException("开始时间必须早于结束时间");
		}
	}

	/** 转 VO，填充 scopeName */
	private PromotionVO toVO(Promotion promotion) {
		PromotionVO vo = new PromotionVO();
		BeanUtils.copyProperties(promotion, vo);
		switch (promotion.getScope()) {
			case 1 -> vo.setScopeName("全场");
			case 2 -> {
				Category category = categoryMapper.selectById(promotion.getScopeId());
				vo.setScopeName(category != null ? category.getName() : "未知分类");
			}
			case 3 -> {
				Product product = productMapper.selectById(promotion.getScopeId());
				vo.setScopeName(product != null ? product.getName() : "未知商品");
			}
		}
		if(LocalDateTime.now().isBefore(promotion.getStartTime())){
			vo.setStage(PromotionStageConstant.NOT_STARTED);
		} else if (LocalDateTime.now().isAfter(promotion.getEndTime())) {
			vo.setStage(PromotionStageConstant.ENDED);
		} else {
			vo.setStage(PromotionStageConstant.ONGOING);
		}
		return vo;
	}

	public void clearHomeSectionAllCache(){
		List<String> keys = new ArrayList<>();
		for (int i = 1; i <= 3; i++) {
			keys.add(HOME_MANUAL_SECTION_KEY_PREFIX + i);
			keys.add(HOME_AUTO_SECTION_KEY_PREFIX + i);
		}
		stringRedisTemplate.delete(keys);
	}
}




