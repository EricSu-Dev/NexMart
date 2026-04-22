package com.nex.nexmart.service.intf.product;

import com.nex.nexmart.common.PageResult;
import com.nex.nexmart.model.entity.product.ProductFavorite;
import com.baomidou.mybatisplus.extension.service.IService;
import com.nex.nexmart.model.vo.product.FavoriteProductVO;

import java.util.List;

/**
* @author Eric
* @description 针对表【product_favorite(商品收藏表)】的数据库操作Service
* @createDate 2026-04-03 17:42:28
*/
public interface ProductFavoriteService extends IService<ProductFavorite> {
	void addFavorite(Long userId, Long productId);
	void removeFavoriteBatch(Long userId, List<Long> productIds);
	PageResult<FavoriteProductVO> getFavoritePage(Long userId, int page, int size,String keyword,Long categoryId);
	boolean isFavorite(Long userId, Long productId);
	Boolean toggle(Long userId, Long productId);
}
