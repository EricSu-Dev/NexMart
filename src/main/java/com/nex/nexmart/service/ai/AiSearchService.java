package com.nex.nexmart.service.ai;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.nex.nexmart.mapper.base.CategoryMapper;
import com.nex.nexmart.model.entity.Category;
import com.nex.nexmart.model.entity.product.Product;
import com.nex.nexmart.model.vo.AiSearchSuggestVO;
import com.nex.nexmart.model.vo.product.ProductVO;
import com.nex.nexmart.service.intf.product.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AiSearchService {

	private final AiContextService aiContextService;
	private final CategoryMapper categoryMapper;
	private final ProductService productService;

	public AiSearchSuggestVO suggest(String keyword) {
	    // 判断搜索类型
		String type = aiContextService.judgeSearchType(keyword);
		List<Product> products;
		//如果是关键字是分类
		if ("category".equals(type)) {
			// 模糊匹配分类
			List<Category> categories = categoryMapper.selectList(
					new LambdaQueryWrapper<Category>()
							.like(Category::getName, keyword)
			);
			if (!categories.isEmpty()) {
				// 匹配到分类，查分类下商品
				List<Long> categoryIds = categories.stream()
						.map(Category::getId)
						.collect(Collectors.toList());
				products = productService.lambdaQuery()
						.in(Product::getCategoryId, categoryIds)
						.eq(Product::getStatus, 1)
						.orderByDesc(Product::getSales)
						.last("limit 8")
						.list();
			} else {
				// 匹配不到，降级走关键词扩展
				List<String> expandedKeywords = aiContextService.expandKeywords(keyword);
				products = aiContextService.searchByExpandedKeywords(expandedKeywords);
			}
		} else {
			// 单品搜索，直接走关键词扩展
			List<String> expandedKeywords = aiContextService.expandKeywords(keyword);
			products = aiContextService.searchByExpandedKeywords(expandedKeywords);
		}

		AiSearchSuggestVO vo = new AiSearchSuggestVO();
		vo.setMessage(buildMessage(keyword, products));
		vo.setProducts(products.stream().map(this::toVO).toList());
		return vo;
	}

	private String buildMessage(String keyword, List<Product> products) {
		if (products.isEmpty()) {
			return "很抱歉，商城暂时没有与\"" + keyword + "\"相关的商品，欢迎浏览其他商品～";
		}
		return "未找到\"" + keyword + "\"的精确匹配，为您推荐以下相关商品";
	}

	private ProductVO toVO(Product p) {
		ProductVO vo = new ProductVO();
		vo.setId(p.getId());
		vo.setName(p.getName());
		vo.setPrice(p.getPrice());
		vo.setCoverUrl(p.getCoverUrl());
		vo.setStock(p.getStock());
		return vo;
	}
}
