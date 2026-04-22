package com.nex.nexmart.service.intf.product;

import com.baomidou.mybatisplus.extension.service.IService;
import com.nex.nexmart.common.PageResult;
import com.nex.nexmart.model.dto.product.ProductDTO;
import com.nex.nexmart.model.entity.product.Product;
import com.nex.nexmart.model.vo.product.ProductVO;
import jakarta.validation.Valid;

import java.math.BigDecimal;
import java.util.List;

/**
 * 商品服务
 */
public interface ProductService extends IService<Product> {

	PageResult<ProductVO> pageProducts(long current, long size, String keyword, Long categoryId,
	                                   Integer status, BigDecimal minPrice, BigDecimal maxPrice,
	                                   String sortBy, String sortOrder, Integer role);

	ProductVO getProductDetail(Long id);

	void addProduct(@Valid ProductDTO dto);

	void updateProduct(Long id, @Valid ProductDTO dto);

	void deleteProduct(Long id);

	void updateProductStatus(Long id, Integer status);


	List<Long> extractCategoryIds(List<Product> products);
}
