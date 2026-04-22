package com.nex.nexmart.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nex.nexmart.exception.BusinessException;
import com.nex.nexmart.mapper.base.CartItemMapper;
import com.nex.nexmart.model.dto.CartAddDTO;
import com.nex.nexmart.model.entity.CartItem;
import com.nex.nexmart.model.entity.Promotion;
import com.nex.nexmart.model.entity.product.Product;
import com.nex.nexmart.model.entity.product.ProductSpec;
import com.nex.nexmart.model.vo.CartVO;
import com.nex.nexmart.service.intf.CartItemService;
import com.nex.nexmart.service.intf.PromotionService;
import com.nex.nexmart.service.intf.product.ProductService;
import com.nex.nexmart.service.intf.product.ProductSpecService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 购物车服务实现
 */
@Service
@RequiredArgsConstructor
public class CartItemServiceImpl extends ServiceImpl<CartItemMapper, CartItem> implements CartItemService {

	private final ProductService productService;
	private final ProductSpecService productSpecService;
	private final PromotionService promotionService;


//	数据库 cart_item 表
//        │
//		        │ lambdaQuery
//        ▼
//	List<CartItem>          ← 只有 productId，没有商品详情
//        │
//		        │ 提取所有 productId
//        ▼
//	List<Long> productIds
//        │
//		        │ IN 查询一次商品表（避免 N+1）
//			▼
//	Map<Long, Product>      ← id → Product 的快速查找表
//        │
//		        │ 组装
//        ▼
//	List<CartVO>            ← CartItem 字段 + Product 字段 + 计算小计
//        │
//		        │ 返回给前端
//        ▼
//	购物车列表页面
	@Override
	public List<CartVO> listCart(Long userId) {
		List<CartItem> cartItems = lambdaQuery()
				.eq(CartItem::getUserId, userId)
				.eq(CartItem::getIsTemporary, 0)
				.orderByDesc(CartItem::getUpdatedAt)
				.list();

		if (cartItems.isEmpty()) {
			return List.of();
		}
		//批量查询只需要查询一次数据库,给到夯
		//把cartItems转换成id的集合
		List<Long> productIds = cartItems.stream()
				.map(CartItem::getProductId).collect(Collectors.toList());

		List<Product> products = productService.lambdaQuery()
				.in(Product::getId, productIds)
				.list();
		//拿到 Map<Long, Product> 之后，后续可以 O(1) 查找，避免嵌套循环：
		Map<Long, Product> productMap = products.stream()
				.collect(Collectors.toMap(Product::getId, p -> p));
		//为优惠活动匹配做准备
		List<Long> categoryIds = products.stream().map(Product::getCategoryId).distinct().toList();

		List<Long> specIds = cartItems.stream()
				.map(CartItem::getSpecId)
				.filter(Objects::nonNull)
				.collect(Collectors.toList());
		Map<Long, ProductSpec> specMap = specIds.isEmpty() ? Collections.emptyMap()
				: productSpecService.listByIds(specIds)
				.stream()
				.collect(Collectors.toMap(ProductSpec::getId, s -> s));

		// 一次性查出所有当前生效的活动
		List<Promotion> activePromotions = promotionService.getActivePromotionList(productIds, categoryIds);

		return cartItems.stream().map(item -> {
			CartVO vo = new CartVO();
			vo.setId(item.getId());
			vo.setProductId(item.getProductId());
			vo.setQuantity(item.getQuantity());
			//规格
			if (item.getSpecId() != null) {
				vo.setSpecId(item.getSpecId());
				ProductSpec spec = specMap.get(item.getSpecId());
				if(spec != null){
					vo.setSpecName(spec.getSpecName());
				}
			}

			Product product = productMap.get(item.getProductId());
			if (product != null) {
				vo.setProductName(product.getName());
				vo.setCoverUrl(product.getCoverUrl());
				vo.setPrice(product.getPrice());
				vo.setStock(product.getStock());
				vo.setCategoryId(product.getCategoryId());
				vo.setSubtotal(product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));

				Promotion best = promotionService.findBestPromotion(product, activePromotions);
				if (best != null) {
					vo.setPromotionName(best.getName());
					BigDecimal discountedPrice = promotionService.calcDiscountedPrice(product.getPrice(), best);
					vo.setDiscountedPrice(discountedPrice);
					vo.setDiscountedAmount(discountedPrice.multiply(BigDecimal.valueOf(item.getQuantity())));
				}
			}
			return vo;
		}).collect(Collectors.toList());
	}

	@Override
	public void addToCart(@Valid CartAddDTO dto, Long userId) {
		Product product = productService.getById(dto.getProductId());
		if (product == null || product.getStatus() == 0) {
			throw new BusinessException("商品不存在或已下架");
		}
		//问题一：有规格的商品必须传 specId
		if (product.getHasSpec() == 1 && dto.getSpecId() == null) {
			throw new BusinessException("请选择商品规格");
		}

		ProductSpec spec;
		int stock; // 提取库存变量，统一后续判断

		//问题二：没有校验规格是否属于该商品(防止恶意用户传一个其他商品的 specId)
		if (dto.getSpecId() != null) {
			spec = productSpecService.lambdaQuery()
					.eq(ProductSpec::getId, dto.getSpecId())
					.eq(ProductSpec::getProductId, dto.getProductId())
					.one();
			if (spec == null) {
				throw new BusinessException("规格不存在");
			}
			stock = spec.getStock();
		} else {
			// 无规格商品使用商品本身的库存
			stock = product.getStock();
		}
		// 本次请求添加的数量本身就超库存，直接拦截
		if (dto.getQuantity() > stock) {
				throw new BusinessException("该规格商品库存不足");
		}

		CartItem existing = lambdaQuery()
				.eq(CartItem::getUserId, userId)
				.eq(CartItem::getProductId, dto.getProductId())
				.eq(CartItem::getIsTemporary, 0)
				.eq(dto.getSpecId() != null, CartItem::getSpecId, dto.getSpecId())
				.one();
		//购物车存在则增加数量,否则添加
		if (existing != null) {
			int newQty = existing.getQuantity() + dto.getQuantity();
			// 购物车已有数量已经达到库存上限，不允许再加
			if (existing.getQuantity() >= stock) {
				throw new BusinessException("该规格的商品数量已达库存上限");
			}
			// 合并后超出库存，截断到库存上限
			int finalQty = Math.min(newQty, stock);
			lambdaUpdate().eq(CartItem::getId, existing.getId())
					.set(CartItem::getQuantity, finalQty)
					.update();
		} else {
			CartItem item = new CartItem();
			item.setUserId(userId);
			item.setProductId(dto.getProductId());
			item.setQuantity(dto.getQuantity());
			item.setSpecId(dto.getSpecId());
			item.setIsTemporary(0);
			save(item);
		}
	}

	//用户手动修改数量
	@Override
	public void updateQuantity(Long id, Integer quantity, Long userId) {
		if (quantity == null || quantity < 1) {
			throw new BusinessException("数量至少为1");
		}
		CartItem item = getById(id);
		if (item == null || !item.getUserId().equals(userId)) {
			throw new BusinessException("购物车条目不存在");
		}
		// 有规格校验规格库存，无规格校验商品库存
		if (item.getSpecId() != null) {
			ProductSpec spec = productSpecService.getById(item.getSpecId());
			if (spec == null || quantity > spec.getStock()) {
				throw new BusinessException("该规格商品库存不足");
			}
		} else {
			Product product = productService.getById(item.getProductId());
			if (product == null || quantity > product.getStock()) {
				throw new BusinessException("商品库存不足");
			}
		}
		lambdaUpdate().eq(CartItem::getId, id)
				.set(CartItem::getQuantity, quantity)
				.update();
	}

	@Override
	public void removeItem(Long id, Long userId) {
		CartItem item = getById(id);
		if (item == null || !item.getUserId().equals(userId)) {
			throw new BusinessException("购物车条目不存在");
		}
		removeById(id);
	}

	@Override
	public List<CartVO> addAndSelectTemporary(Long productId, Integer quantity, Long specId, Long userId) {
		Product product = productService.getById(productId);
		if (product == null || product.getStatus() == 0) {
			throw new BusinessException("商品不存在或已下架");
		}
		//有规格的商品必须传 specId
		if (product.getHasSpec() == 1 && specId == null) {
			throw new BusinessException("请选择商品规格");
		}

		ProductSpec spec;
		int stock; // 提取库存变量，统一后续判断

		//校验规格是否属于该商品(防止恶意用户传一个其他商品的 specId)
		if (specId != null) {
			spec = productSpecService.lambdaQuery()
					.eq(ProductSpec::getId, specId)
					.eq(ProductSpec::getProductId, productId)
					.one();
			if (spec == null) {
				throw new BusinessException("规格不存在");
			}
			stock = spec.getStock();
		} else {
			// 无规格商品使用商品本身的库存
			stock = product.getStock();
		}
		// 本次请求添加的数量本身就超库存，直接拦截
		if (quantity > stock) {
			throw new BusinessException("该规格商品库存不足");
		}
		//添加到临时购物车
		CartItem item = new CartItem();
		item.setUserId(userId);
		item.setProductId(productId);
		item.setQuantity(quantity);
		if(specId != null){
			item.setSpecId(specId);
		}
		item.setIsTemporary(1);
		save(item);
		//查询临时购物车
		CartItem temp = lambdaQuery().eq(CartItem::getUserId, userId)
				.eq(CartItem::getIsTemporary, 1)
				.eq(CartItem::getProductId, productId)
				.eq(CartItem::getQuantity, quantity)
				.one();
		if (temp == null) {
			throw new BusinessException("添加并查询临时购物车失败");
		}
		CartVO vo = new CartVO();
		BeanUtils.copyProperties(temp, vo);
		Product tempProduct = productService.getById(productId);
		vo.setProductName(tempProduct.getName());
		vo.setCoverUrl(tempProduct.getCoverUrl());
		vo.setPrice(tempProduct.getPrice());
		vo.setStock(tempProduct.getStock());
		vo.setCategoryId(tempProduct.getCategoryId());
		if (temp.getSpecId() != null) {
			vo.setSpecName(productSpecService.getById(temp.getSpecId()).getSpecName());
		}
		vo.setSubtotal(vo.getPrice().multiply(new BigDecimal(vo.getQuantity())));
		Promotion activePromotion = promotionService.getActivePromotion(productId, tempProduct.getCategoryId(), tempProduct.getPrice());
		if (activePromotion != null) {
			vo.setPromotionName(activePromotion.getName());
			vo.setDiscountedPrice(promotionService.calcDiscountedPrice(tempProduct.getPrice(), activePromotion));
		}
		List<CartVO> list = new ArrayList<>();
		list.add(vo);
		return list;
	}

	@Override
	public void removeTemporary(Long cartItemId, Long userId) {
		CartItem item = getById(cartItemId);
		if (item == null || !item.getUserId().equals(userId)) {
			throw new BusinessException("购物车条目不存在");
		}
		removeById(cartItemId);
	}

	@Override
	public void clearAllTemporary(Long userId) {
		remove(new QueryWrapper<CartItem>().eq("user_id", userId).eq("is_temporary", 1));
	}
}
