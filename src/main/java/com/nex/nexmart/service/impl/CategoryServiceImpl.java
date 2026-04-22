package com.nex.nexmart.service.impl;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nex.nexmart.common.constant.UserRoleConstants;
import com.nex.nexmart.exception.BusinessException;
import com.nex.nexmart.mapper.base.ProductMapper;
import com.nex.nexmart.model.dto.CategoryDTO;
import com.nex.nexmart.model.entity.Category;
import com.nex.nexmart.model.entity.product.Product;
import com.nex.nexmart.service.intf.CategoryService;
import com.nex.nexmart.mapper.base.CategoryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
* @author Eric
*  针对表【category(商品分类表)】的数据库操作Service实现
*  2026-03-26 12:43:10
*/
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService{

	private final ProductMapper productMapper;
	private final StringRedisTemplate stringRedisTemplate;

	private static final String HOME_MANUAL_SECTION_KEY_PREFIX = "NexMart:home:section:manual:";
	private static final String HOME_AUTO_SECTION_KEY_PREFIX = "NexMart:home:section:auto:";
	private static final String CATEGORY_LIST_KEY = "NexMart:category:list";

	@Override
	public List<Category> selectList(Integer role) {
		// 管理端不走缓存，实时查DB
		if (!Objects.equals(role, UserRoleConstants.ROLE_USER)) {
			return lambdaQuery()
					.orderByAsc(Category::getSortOrder)
					.list();
		}
		// 用户端走redis缓存
		String cached = stringRedisTemplate.opsForValue().get(CATEGORY_LIST_KEY);
		if (cached != null) {
			return JSON.parseArray(cached, Category.class);
		}
		// 缓存不存在，查DB并写入缓存
		List<Category> list = lambdaQuery()
				.eq(Category::getStatus, 1)
				.orderByAsc(Category::getSortOrder)
				.list();
		stringRedisTemplate.opsForValue().set(CATEGORY_LIST_KEY, JSON.toJSONString(list),
				3, TimeUnit.HOURS);// 缓存3小时
		return list;
	}

	@Override
	public void addCategory(CategoryDTO dto) {
		Long count = lambdaQuery().eq(Category::getName, dto.getName()).count();
		if (count > 0) {
			throw new BusinessException("分类名称已存在");
		}
		Category category = new Category();
		BeanUtils.copyProperties(dto, category);
		save(category);
		// 删除缓存
		stringRedisTemplate.delete(CATEGORY_LIST_KEY);
	}

	@Override
	public void updateCategory(Long id, CategoryDTO dto) {
		Category category = getById(id);
		if (category == null) {
			throw new BusinessException("分类不存在");
		}
		BeanUtils.copyProperties(dto, category);
		category.setId(id);
		updateById(category);
		stringRedisTemplate.delete(CATEGORY_LIST_KEY);
		clearHomeSectionAllCache();
	}

	@Override
	public void removeCategory(Long id) {
		if (getById(id) == null) {
			throw new BusinessException("分类不存在");
		}
		Long count = productMapper.selectCount(new LambdaQueryWrapper<Product>().eq(Product::getCategoryId, id));
		if (count > 0) {
			throw new BusinessException("该分类下有商品，请先删除商品");
		}
		removeById(id);
		stringRedisTemplate.delete(CATEGORY_LIST_KEY);
		clearHomeSectionAllCache();
	}

	@Override
	public Map<Long, String> getCategoryNameMap(List<Long> categoryIds) {
		return lambdaQuery()
				.in(Category::getId, categoryIds).list()
				.stream()
				.collect(Collectors.toMap(Category::getId, Category::getName));
	}

	public void clearHomeSectionAllCache(){
		List<String> keys = new ArrayList<>();
		for (int i = 1; i <= 3; i++) {
			keys.add(HOME_MANUAL_SECTION_KEY_PREFIX + i);
			keys.add(HOME_AUTO_SECTION_KEY_PREFIX + i);
		}
		stringRedisTemplate.delete(keys);
	}
}




