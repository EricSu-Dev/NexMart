package com.nex.nexmart.util;

import com.nex.nexmart.mapper.base.CategoryMapper;
import com.nex.nexmart.mapper.base.ProductMapper;
import com.nex.nexmart.model.entity.coupon.Coupon;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Component
public class CouponDescHelper {

	@Autowired
	private CategoryMapper categoryMapper;
	@Autowired
	private ProductMapper productMapper;

	public ScopeNameMaps buildScopeNameMaps(List<Coupon> coupons) {
		//批量查询分类名与商品名,首先先分组
		Map<Integer, List<Coupon>> scopeGroupMap = coupons.stream()
				.filter(c -> c.getScopeId() != null)
				.collect(Collectors.groupingBy(Coupon::getScope));
		List<Coupon> categoryCoupons = scopeGroupMap.getOrDefault(2, List.of());
		List<Coupon> productCoupons = scopeGroupMap.getOrDefault(3, List.of());

		//批量查分类名
		Map<Long, String> categoryNameMap = new HashMap<>();
		if (!categoryCoupons.isEmpty()) {
			List<Long> categoryIds = categoryCoupons.stream()
					.map(Coupon::getScopeId).distinct().toList();
			categoryMapper.selectBatchIds(categoryIds)
					.forEach(c -> categoryNameMap.put(c.getId(), c.getName()));
		}

		//批量查方法名
		Map<Long, String> productNameMap = new HashMap<>();
		if (!productCoupons.isEmpty()) {
			List<Long> productIds = productCoupons.stream()
					.map(Coupon::getScopeId).distinct().toList();
			productMapper.selectBatchIds(productIds)
					.forEach(p -> productNameMap.put(p.getId(), p.getName()));
		}

		return new ScopeNameMaps(categoryNameMap, productNameMap);
	}

	public void fillCommonDesc(Coupon coupon,
	                           Consumer<String> couponTypeDescSetter,
	                           Consumer<String> discountTypeDescSetter,
	                           Consumer<String> scopeDescSetter,
	                           Consumer<String> scopeNameSetter,
	                           Map<Long, String> categoryNameMap,
	                           Map<Long, String> productNameMap) {
		if (couponTypeDescSetter != null) {
			couponTypeDescSetter.accept(switch (coupon.getCouponType()) {
				case 1 -> "商品券";
				case 2 -> "秒杀订单券";
				default -> "未知";
			});
		}

		discountTypeDescSetter.accept(switch (coupon.getDiscountType()) {
			case 1 -> "满减";
			case 2 -> "折扣";
			case 3 -> "无门槛";
			default -> "未知";
		});

		if (coupon.getCouponType() == 1) {
			scopeDescSetter.accept(switch (coupon.getScope()) {
				case 1 -> "全场";
				case 2 -> "单分类";
				case 3 -> "单商品";
				default -> "未知";
			});
			if (coupon.getScope() == 2 && coupon.getScopeId() != null) {
				scopeNameSetter.accept(categoryNameMap.get(coupon.getScopeId()));
			} else if (coupon.getScope() == 3 && coupon.getScopeId() != null) {
				scopeNameSetter.accept(productNameMap.get(coupon.getScopeId()));
			}
		} else {
			scopeDescSetter.accept("全场");
		}
	}

	@Data
	@AllArgsConstructor
	public static class ScopeNameMaps {
		private Map<Long, String> categoryNameMap;
		private Map<Long, String> productNameMap;
	}
}
