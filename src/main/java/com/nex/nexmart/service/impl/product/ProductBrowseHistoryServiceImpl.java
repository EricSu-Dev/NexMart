package com.nex.nexmart.service.impl.product;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nex.nexmart.common.PageResult;
import com.nex.nexmart.mapper.base.ProductMapper;
import com.nex.nexmart.model.entity.Promotion;
import com.nex.nexmart.model.entity.product.ProductBrowseHistory;
import com.nex.nexmart.model.vo.product.BrowseHistoryVO;
import com.nex.nexmart.service.intf.PromotionService;
import com.nex.nexmart.service.intf.product.ProductBrowseHistoryService;
import com.nex.nexmart.mapper.ProductBrowseHistoryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author Eric
 *  针对表【product_browse_history(商品浏览记录表)】的数据库操作Service实现
 *  2026-04-04 22:02:54
 */
@RequiredArgsConstructor
@Service
public class ProductBrowseHistoryServiceImpl extends ServiceImpl<ProductBrowseHistoryMapper, ProductBrowseHistory> implements ProductBrowseHistoryService {
	private final ProductBrowseHistoryMapper browseHistoryMapper;
	private final ProductMapper productMapper;
	private final PromotionService promotionService;
	private final StringRedisTemplate redisTemplate;
	private static final String DEDUP_KEY_PREFIX = "NexMart:browse_product:dedup:";


	@Override
	public void record(Long userId, Long productId) {
		if (productMapper.selectById(productId) == null) {
			throw new RuntimeException("商品不存在");
		}
		String dedupKey = DEDUP_KEY_PREFIX + userId + ":" + productId;
		//10分钟内不允许重复更新同一商品的浏览记录,减轻数据库压力
		if (!redisTemplate.hasKey(dedupKey)) {
			int affected = browseHistoryMapper.insertOrUpdate(userId, productId);
			redisTemplate.opsForValue().set(dedupKey, "1", 10, TimeUnit.MINUTES);
			//如果是新数据插入的话，就去除最旧的数据
			if (affected == 1) {
				trimAsync(userId);
			}
		}
	}

	@Override
	public PageResult<BrowseHistoryVO> getHistoryPage(Long userId, int page, int size,String keyword, Long categoryId) {
		IPage<BrowseHistoryVO> IPage = browseHistoryMapper.selectHistoryPage(new Page<>(page, size), userId, keyword, categoryId);
		List<BrowseHistoryVO> browseHistoryVOs = IPage.getRecords();
		if (!browseHistoryVOs.isEmpty()) {
			// 批量拿到所有 productId 和 categoryId
			List<Long> productIds = browseHistoryVOs.stream()
					.map(BrowseHistoryVO::getProductId).toList();
			List<Long> categoryIds = browseHistoryVOs.stream()
					.map(BrowseHistoryVO::getCategoryId).toList();
			List<Promotion> activePromotions = promotionService.getActivePromotionList(productIds, categoryIds);
			browseHistoryVOs.forEach(vo->{
				Promotion best = promotionService.findBestPromotion(vo, activePromotions);
				if (best != null) {
					vo.setPromotionName(best.getName());
					vo.setDiscountedPrice(promotionService.calcDiscountedPrice(vo.getPrice(), best));
				}
			});
		}
		return PageResult.of(browseHistoryVOs,IPage.getTotal(),IPage.getCurrent(),IPage.getSize());
	}


	@Override
	public void removeOne(Long userId, Long id) {
		remove(new LambdaQueryWrapper<ProductBrowseHistory>()
				.eq(ProductBrowseHistory::getId, id)
				.eq(ProductBrowseHistory::getUserId, userId));
	}

	@Override
	public void removeAll(Long userId) {
		remove(new LambdaQueryWrapper<ProductBrowseHistory>()
				.eq(ProductBrowseHistory::getUserId, userId));
	}

	@Async
	public void trimAsync(Long userId) {
		long count = lambdaQuery().eq(ProductBrowseHistory::getUserId, userId).count();
		if (count > 100) {
			browseHistoryMapper.deleteOldRecords(userId, 100);
		}
	}
}





