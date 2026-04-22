package com.nex.nexmart.service.impl.product;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nex.nexmart.exception.BusinessException;
import com.nex.nexmart.model.entity.product.ProductSpec;
import com.nex.nexmart.service.intf.product.ProductSpecService;
import com.nex.nexmart.mapper.base.ProductSpecMapper;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.stream.Collectors;

/**
* @author Eric
* 针对表【product_spec(商品规格)】的数据库操作Service实现
* 2026-03-29 22:35:09
*/
@Service
public class ProductSpecServiceImpl extends ServiceImpl<ProductSpecMapper, ProductSpec> implements ProductSpecService{
	@Override
	public Map<Long,String> getSpecNameMap(Long productId){
		if(productId==null){
			throw  new BusinessException("商品ID不能为空");
		}
		 return lambdaQuery().eq(ProductSpec::getProductId, productId)
				.select(ProductSpec::getId, ProductSpec::getSpecName)
				.list()
				.stream()
				.collect(Collectors.toMap(ProductSpec::getId, ProductSpec::getSpecName));
	}
}




