package com.nex.nexmart.controller.user;

import com.nex.nexmart.common.Result;
import com.nex.nexmart.model.dto.CartAddDTO;
import com.nex.nexmart.model.vo.CartVO;
import com.nex.nexmart.service.intf.CartItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.nex.nexmart.security.SecurityUserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "用户端-购物车接口")
@RestController
@RequestMapping("/api/user/cart")
@RequiredArgsConstructor
@Slf4j
public class CartController {

	private final CartItemService cartItemService;

	@Operation(summary = "查询购物车列表")
	@GetMapping("/list")
	public Result<List<CartVO>> list(@AuthenticationPrincipal SecurityUserDetails userDetails) {
		Long userId = userDetails.getUser().getId();
		log.info("Cart list userId={}", userId);
		return Result.success(cartItemService.listCart(userId));
	}

	@Operation(summary = "加入购物车（已存在则追加数量）")
	@PostMapping
	public Result<Void> add(@Valid @RequestBody CartAddDTO dto, @AuthenticationPrincipal SecurityUserDetails userDetails) {
		Long userId = userDetails.getUser().getId();
		log.info("Cart add userId={} productId={} quantity={}", userId, dto.getProductId(), dto.getQuantity());
		cartItemService.addToCart(dto, userId);
		return Result.success();
	}

	@Operation(summary = "修改购物车商品数量")
	@PutMapping("/{id}")
	public Result<Void> update(@PathVariable Long id,
	                           @RequestParam Integer quantity,
	                           @AuthenticationPrincipal SecurityUserDetails userDetails) {
		Long userId = userDetails.getUser().getId();
		log.info("Cart update id={} userId={} quantity={}", id, userId, quantity);
		cartItemService.updateQuantity(id, quantity, userId);
		return Result.success();
	}

	@Operation(summary = "从购物车移除")
	@DeleteMapping("/{id}")
	public Result<Void> delete(@PathVariable Long id, @AuthenticationPrincipal SecurityUserDetails userDetails) {
		Long userId = userDetails.getUser().getId();
		log.info("Cart delete id={} userId={}", id, userId);
		cartItemService.removeItem(id, userId);
		return Result.success();
	}

	@Operation(summary = "临时加入并查看购物车")
	@PostMapping("/temporary")
	public Result<List<CartVO>> addAndSelectTemporary(@RequestParam Long productId,@RequestParam Integer quantity,@RequestParam(required = false) Long specId,
	                                 @AuthenticationPrincipal SecurityUserDetails userDetails){
		Long userId = userDetails.getUser().getId();
		log.info("临时加入购物车 userId={} productId={} quantity={}", userId, productId, quantity);
		return Result.success(cartItemService.addAndSelectTemporary(productId,quantity,specId,userId));
	}

	@Operation(summary = "临时移除购物车项")
	@DeleteMapping("/temporary/{cartItemId}")
	public Result<Void> removeTemporary(@PathVariable Long cartItemId, @AuthenticationPrincipal SecurityUserDetails userDetails){
		Long userId = userDetails.getUser().getId();
		log.info("移除id为{}的购物车项", cartItemId);
		cartItemService.removeTemporary(cartItemId, userId);
		return Result.success();
	}

	@DeleteMapping("/temporary/clearAll")
	@Operation(summary = "清空临时购物车")
	public Result<Void> clearAllTemporary(@AuthenticationPrincipal SecurityUserDetails userDetails){
		Long userId = userDetails.getUser().getId();
		log.info("清空临时购物车项");
		cartItemService.clearAllTemporary(userId);
		return Result.success();
	}

}
