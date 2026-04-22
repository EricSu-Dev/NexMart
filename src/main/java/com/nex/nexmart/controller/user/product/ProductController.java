package com.nex.nexmart.controller.user.product;

import com.nex.nexmart.common.constant.UserRoleConstants;
import com.nex.nexmart.common.PageResult;
import com.nex.nexmart.common.Result;
import com.nex.nexmart.model.vo.product.ProductVO;
import com.nex.nexmart.security.SecurityUserDetails;
import com.nex.nexmart.service.intf.product.ProductService;
import com.nex.nexmart.service.intf.product.SearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Objects;

@Tag(name = "用户端-商品接口")
@RestController("userProductController")
@RequestMapping("/api/user/product")
@RequiredArgsConstructor
@Slf4j
public class ProductController {

	private final ProductService productService;
	private final SearchService searchService;

	@Operation(summary = "分页查询商品（公开）")
	@GetMapping("/page")
	public Result<PageResult<ProductVO>> page(
			@RequestParam(defaultValue = "1") long current,
			@RequestParam(defaultValue = "10") long size,
			@RequestParam(required = false) String keyword,
			@RequestParam(required = false) Long categoryId,
			@RequestParam(required = false) BigDecimal minPrice,
			@RequestParam(required = false) BigDecimal maxPrice,
			@RequestParam(required = false) String sortBy,
			@RequestParam(required = false) String sortOrder,
			@AuthenticationPrincipal SecurityUserDetails userDetails) {
		log.info("Product page current={} size={} keyword={} categoryId={} minPrice={} maxPrice={} sortBy={} sortOrder={}",
				current, size, keyword, categoryId, minPrice, maxPrice, sortBy, sortOrder);
		Integer role = userDetails == null ? UserRoleConstants.ROLE_USER : userDetails.getUser().getRole();
		// 记录搜索关键词
		if (StringUtils.hasText(keyword) && Objects.equals(role, UserRoleConstants.ROLE_USER)) {
			searchService.recordKeyword(keyword);
		}
		return Result.success(productService.pageProducts(
				current, size, keyword, categoryId,
				null, minPrice, maxPrice, sortBy, sortOrder, role
		));
	}

	@Operation(summary = "商品详情（公开）")
	@GetMapping("/{id}")
	public Result<ProductVO> detail(@PathVariable Long id) {
		log.info("Product detail id={}", id);
		return Result.success(productService.getProductDetail(id));
	}
}
