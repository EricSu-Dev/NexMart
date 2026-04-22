package com.nex.nexmart.service.intf.product;

import com.nex.nexmart.model.entity.product.ProductSpec;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Map;

/**
* @author Eric
*  针对表【product_spec(商品规格)】的数据库操作Service
*  2026-03-29 22:35:09
*/
public interface ProductSpecService extends IService<ProductSpec> {
	Map<Long,String> getSpecNameMap(Long productId);
}
