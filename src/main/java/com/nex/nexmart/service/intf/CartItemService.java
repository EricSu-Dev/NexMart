package com.nex.nexmart.service.intf;

import com.baomidou.mybatisplus.extension.service.IService;
import com.nex.nexmart.model.dto.CartAddDTO;
import com.nex.nexmart.model.entity.CartItem;
import com.nex.nexmart.model.vo.CartVO;
import jakarta.validation.Valid;

import java.util.List;

/**
 * 购物车服务
 */
public interface CartItemService extends IService<CartItem> {

	List<CartVO> listCart(Long userId);

	void addToCart(@Valid CartAddDTO dto, Long userId);

	void updateQuantity(Long id, Integer quantity, Long userId);

	void removeItem(Long id, Long userId);

	List<CartVO> addAndSelectTemporary(Long productId, Integer quantity, Long specId, Long userId);

	void removeTemporary(Long cartItemId, Long userId);

	void clearAllTemporary(Long userId);

}
