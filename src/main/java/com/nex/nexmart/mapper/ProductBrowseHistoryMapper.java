package com.nex.nexmart.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nex.nexmart.model.entity.product.ProductBrowseHistory;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nex.nexmart.model.vo.product.BrowseHistoryVO;
import org.apache.ibatis.annotations.Param;

/**
* @author Eric
* @description 针对表【product_browse_history(商品浏览记录表)】的数据库操作Mapper
* @createDate 2026-04-04 22:02:54
* @Entity com.nex.nexmart.model.entity.product.ProductBrowseHistory
*/
public interface ProductBrowseHistoryMapper extends BaseMapper<ProductBrowseHistory> {

	int insertOrUpdate(@Param("userId") Long userId, @Param("productId") Long productId);

	IPage<BrowseHistoryVO> selectHistoryPage(Page<BrowseHistoryVO> page,
	                                         @Param("userId") Long userId,
	                                         @Param("keyword") String keyword,
	                                         @Param("categoryId") Long categoryId);

	void deleteOldRecords(@Param("userId") Long userId, @Param("max") int max);
}




