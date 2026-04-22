package com.nex.nexmart.service.intf;

import com.nex.nexmart.model.dto.CategoryDTO;
import com.nex.nexmart.model.entity.Category;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.validation.Valid;

import java.util.List;
import java.util.Map;

/**
* @author Eric
* @description 针对表【category(商品分类表)】的数据库操作Service
* @createDate 2026-03-26 12:43:10
*/
public interface CategoryService extends IService<Category> {

	List<Category> selectList(Integer role);

	void addCategory(@Valid CategoryDTO dto);

	void updateCategory(Long id, @Valid CategoryDTO dto);

	void removeCategory(Long id);

	Map<Long, String> getCategoryNameMap(List<Long> categoryIds);
}
