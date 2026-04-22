package com.nex.nexmart.controller.admin;

import com.nex.nexmart.common.PageResult;
import com.nex.nexmart.common.Result;
import com.nex.nexmart.model.dto.product.ProductDTO;
import com.nex.nexmart.model.vo.product.ProductVO;
import com.nex.nexmart.security.SecurityUserDetails;
import com.nex.nexmart.service.intf.product.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@Tag(name = "管理端-商品接口")
@RestController("adminProductController")
@RequestMapping("/api/admin/product")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'BOSS')")
@Slf4j
public class ProductController {

	private final ProductService productService;

	@Operation(summary = "管理员分页查询商品")
	@GetMapping("/page")
	public Result<PageResult<ProductVO>> page(
			@RequestParam(defaultValue = "1") long current,
			@RequestParam(defaultValue = "10") long size,
			@RequestParam(required = false) String keyword,
			@RequestParam(required = false) Long categoryId,
			@RequestParam(required = false) Integer status,
			@RequestParam(required = false) BigDecimal minPrice,
			@RequestParam(required = false) BigDecimal maxPrice,
			@RequestParam(required = false) String sortBy,
			@RequestParam(required = false) String sortOrder,
			@AuthenticationPrincipal SecurityUserDetails userDetails) {
		log.info("Product page current={} size={} keyword={} categoryId={} status={} minPrice={} maxPrice={} sortBy={} sortOrder={}",
				current, size, keyword, categoryId, status, minPrice, maxPrice, sortBy, sortOrder);
		return Result.success(productService.pageProducts(
				current, size, keyword, categoryId, status, minPrice, maxPrice, sortBy, sortOrder,
				userDetails.getUser().getRole()
		));
	}

	@Operation(summary = "管理员获取商品详情")
	@GetMapping("/{id}")
	public Result<ProductVO> detail(@PathVariable Long id) {
		return Result.success(productService.getProductDetail(id));
	}

	@Operation(summary = "新增商品")
	@PostMapping
	public Result<Void> add(@Valid @RequestBody ProductDTO dto) {
		log.info("Product add name={} categoryId={}", dto.getName(), dto.getCategoryId());
		productService.addProduct(dto);
		return Result.success();
	}

	@Operation(summary = "修改商品")
	@PutMapping("/{id}")
	public Result<Void> update(@PathVariable Long id, @Valid @RequestBody ProductDTO dto) {
		log.info("Product update id={} name={} categoryId={}", id, dto.getName(), dto.getCategoryId());
		productService.updateProduct(id, dto);
		return Result.success();
	}

	@Operation(summary = "删除商品")
	@DeleteMapping("/{id}")
	public Result<Void> delete(@PathVariable Long id) {
		log.info("Product delete id={}", id);
		productService.deleteProduct(id);
		return Result.success();
	}

	@PutMapping("/{id}/{status}")
	@Operation(summary = "修改商品状态")
	public Result<Void> status(@PathVariable Long id, @PathVariable Integer status){
		log.info("更新商品状态!");
		productService.updateProductStatus(id, status);
		return Result.success();
	}
}
