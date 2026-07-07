package com.nex.nexmart.service.impl;

import com.alibaba.fastjson.JSON;
import com.nex.nexmart.common.constant.UserRoleConstants;
import com.nex.nexmart.mapper.base.ProductMapper;
import com.nex.nexmart.model.entity.Category;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class CategoryServiceImplTest {

	@Test
	void selectListReadsUserCategoryListFromRedisCache() {
		ProductMapper productMapper = mock(ProductMapper.class);
		StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
		@SuppressWarnings("unchecked")
		ValueOperations<String, String> valueOperations = mock(ValueOperations.class);
		Category cachedCategory = new Category();
		cachedCategory.setId(10L);
		cachedCategory.setName("Phone");
		cachedCategory.setStatus(1);

		when(redisTemplate.opsForValue()).thenReturn(valueOperations);
		when(valueOperations.get("NexMart:category:list"))
				.thenReturn(JSON.toJSONString(List.of(cachedCategory)));

		CategoryServiceImpl categoryService = new CategoryServiceImpl(productMapper, redisTemplate);

		List<Category> result = categoryService.selectList(UserRoleConstants.ROLE_USER);

		assertThat(result).hasSize(1);
		assertThat(result.get(0).getId()).isEqualTo(10L);
		assertThat(result.get(0).getName()).isEqualTo("Phone");
		verify(valueOperations, never()).set(anyString(), anyString(), anyLong(), org.mockito.ArgumentMatchers.any(TimeUnit.class));
		verifyNoInteractions(productMapper);
	}
}
