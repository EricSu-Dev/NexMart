package com.nex.nexmart.service.intf.product;

import com.nex.nexmart.common.PageResult;
import com.nex.nexmart.model.entity.product.ProductBrowseHistory;
import com.baomidou.mybatisplus.extension.service.IService;
import com.nex.nexmart.model.vo.product.BrowseHistoryVO;

/**
* @author Eric
* @description 针对表【product_browse_history(商品浏览记录表)】的数据库操作Service
* @createDate 2026-04-04 22:02:54
*/
public interface ProductBrowseHistoryService extends IService<ProductBrowseHistory> {
		void record(Long userId, Long productId);
		PageResult<BrowseHistoryVO> getHistoryPage(Long userId, int page, int size,String keyword, Long categoryId);
		void removeOne(Long userId, Long id);
		void removeAll(Long userId);
}
