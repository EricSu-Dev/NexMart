package com.nex.nexmart.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nex.nexmart.model.entity.product.ProductFavorite;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nex.nexmart.model.vo.product.FavoriteProductVO;
import org.apache.ibatis.annotations.Param;

/**
* @author Eric
* @description 针对表【product_favorite(商品收藏表)】的数据库操作Mapper
* @createDate 2026-04-03 17:42:28
* @Entity com.nex.nexmart.model.entity.product.ProductFavorite
*/
public interface ProductFavoriteMapper extends BaseMapper<ProductFavorite> {
	IPage<FavoriteProductVO> selectFavoritePage(Page<FavoriteProductVO> page,
	                                            @Param("userId") Long userId,
	                                            @Param("keyword") String keyword,
	                                            @Param("categoryId") Long categoryId);
}




