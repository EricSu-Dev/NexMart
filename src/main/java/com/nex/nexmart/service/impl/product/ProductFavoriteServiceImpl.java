package com.nex.nexmart.service.impl.product;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nex.nexmart.common.PageResult;
import com.nex.nexmart.model.entity.Promotion;
import com.nex.nexmart.model.entity.product.ProductFavorite;
import com.nex.nexmart.model.vo.product.FavoriteProductVO;
import com.nex.nexmart.service.intf.PromotionService;
import com.nex.nexmart.service.intf.product.ProductFavoriteService;
import com.nex.nexmart.mapper.ProductFavoriteMapper;
import com.nex.nexmart.service.intf.product.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
* @author Eric
* @description 针对表【product_favorite(商品收藏表)】的数据库操作Service实现
* @createDate 2026-04-03 17:42:28
*/
@Service
@RequiredArgsConstructor
public class ProductFavoriteServiceImpl extends ServiceImpl<ProductFavoriteMapper, ProductFavorite> implements ProductFavoriteService{
	private final ProductFavoriteMapper productFavoriteMapper;
	private final ProductService productService;
	private final PromotionService promotionService;

	@Override
	public void addFavorite(Long userId, Long productId) {
		// 检查商品是否存在
		if (productService.getById(productId) == null) {
			throw new RuntimeException("商品不存在");
		}
		// 已收藏则忽略（利用唯一索引也能防重，这里提前判断给友好提示）
		Long count = lambdaQuery().eq(ProductFavorite::getUserId, userId)
				.eq(ProductFavorite::getProductId, productId)
				.count();
		if (count > 0) return;

		ProductFavorite favorite = new ProductFavorite();
		favorite.setUserId(userId);
		favorite.setProductId(productId);
		save(favorite);
	}

	@Override
	public void removeFavoriteBatch(Long userId, List<Long> productIds) {
		remove(new LambdaQueryWrapper<ProductFavorite>()
				.eq(ProductFavorite::getUserId, userId)
				.in(ProductFavorite::getProductId, productIds)
		);
	}

	@Override
	public PageResult<FavoriteProductVO> getFavoritePage(Long userId, int page, int size, String keyword, Long categoryId) {
		Page<FavoriteProductVO> pageParam = new Page<>(page, size);
		IPage<FavoriteProductVO> IPage = productFavoriteMapper.selectFavoritePage(pageParam, userId, keyword, categoryId);
		List<FavoriteProductVO> favoriteProductVOs = IPage.getRecords();
		if (!favoriteProductVOs.isEmpty()) {
			// 批量拿到所有 productId 和 categoryId
			List<Long> productIds = favoriteProductVOs.stream()
					.map(FavoriteProductVO::getProductId).toList();
			List<Long> categoryIds = favoriteProductVOs.stream()
					.map(FavoriteProductVO::getCategoryId).toList();
			List<Promotion> activePromotions = promotionService.getActivePromotionList(productIds, categoryIds);
			favoriteProductVOs.forEach(vo -> {
				Promotion best = promotionService.findBestPromotion(vo, activePromotions);
				if (best != null) {
					vo.setPromotionName(best.getName());
					vo.setDiscountedPrice(promotionService.calcDiscountedPrice(vo.getPrice(), best));
				}
			});
		}
		return PageResult.of(favoriteProductVOs, IPage.getTotal(), IPage.getCurrent(), IPage.getSize());
	}

	@Override
	public boolean isFavorite(Long userId, Long productId) {
		return lambdaQuery().eq(ProductFavorite::getUserId, userId)
				.eq(ProductFavorite::getProductId, productId)
				.count()>0;
	}

	//   收藏 / 取消收藏
	@Override
	public Boolean toggle(Long userId, Long productId) {
		boolean current = isFavorite(userId, productId);
		if (current) {
			remove(new LambdaQueryWrapper<ProductFavorite>()
					.eq(ProductFavorite::getUserId, userId)
					.eq(ProductFavorite::getProductId, productId
					));
		} else {
			addFavorite(userId, productId);
		}
		return !current;
	}
}




