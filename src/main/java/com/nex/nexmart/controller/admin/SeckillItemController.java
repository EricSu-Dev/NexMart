package com.nex.nexmart.controller.admin;

import com.nex.nexmart.common.PageResult;
import com.nex.nexmart.common.Result;
import com.nex.nexmart.model.dto.seckill.AddSeckillItemDTO;
import com.nex.nexmart.model.vo.seckill.SeckillCouponItemVO;
import com.nex.nexmart.model.vo.seckill.SeckillProductItemVO;
import com.nex.nexmart.service.impl.product.ProductSpecServiceImpl;
import com.nex.nexmart.service.intf.seckill.SeckillItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@Slf4j
@Tag(name = "管理端-秒杀项管理")
@RestController("AdminSeckillItemController")
@RequestMapping("/api/admin/seckill/item")
@RequiredArgsConstructor
public class SeckillItemController {

	private final SeckillItemService seckillItemService;
	private final ProductSpecServiceImpl productSpecServiceImpl;

	@GetMapping("/product/list")
	@Operation(summary = "获取秒杀商品列表")
	public Result<PageResult<SeckillProductItemVO>> productList
			(Integer current, Integer size,
			 @RequestParam(required = false, defaultValue = "false") Boolean onlyUnbound,
			 @RequestParam(required = false) Long activityId) {
		return Result.success(seckillItemService.productList(current, size, onlyUnbound, activityId));
	}

	@GetMapping("/coupon/list")
	@Operation(summary = "获取秒杀订单券列表")
	public Result<PageResult<SeckillCouponItemVO>> couponList
			(Integer current, Integer size,
			 @RequestParam(required = false, defaultValue = "false") Boolean onlyUnbound,
			 @RequestParam(required = false) Long activityId) {
		return Result.success(seckillItemService.couponList(current, size, onlyUnbound, activityId));
	}

	@PostMapping
	@Operation(summary = "添加秒杀项")
	public Result<Void> add(@RequestBody @Validated AddSeckillItemDTO dto) {
		log.info("添加秒杀商品: {}", dto);
		seckillItemService.addItem(dto);
		return Result.success();
	}

	@GetMapping("/spec/{productId}")
	@Operation(summary = "获取秒杀商品规格")
	public Result<Map<Long, String>> getProductSpec(@PathVariable Long productId) {
		log.info("获取秒杀商品的规格: {}", productId);
		return Result.success(productSpecServiceImpl.getSpecNameMap(productId));
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "删除秒杀项")
	public Result<Void> remove(@PathVariable Long id) {
		log.info("删除秒杀商品: {}", id);
		seckillItemService.removeItem(id);
		return Result.success();
	}

	@PutMapping("/{id}")
	@Operation(summary = "更新秒杀项")
	public Result<Void> updateItem(@PathVariable Long id, @RequestParam BigDecimal seckillPrice, @RequestParam Integer perLimit) {
		log.info("更新秒杀商品: {}", id);
		seckillItemService.updateItem(id, seckillPrice, perLimit);
		return Result.success();
	}

	@PatchMapping("/{id}/status")
	@Operation(summary = "上下架秒杀项")
	public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
		log.info("上下架秒杀商品: {}, {}", id, status);
		seckillItemService.updateStatus(id, status);
		return Result.success();
	}

}
