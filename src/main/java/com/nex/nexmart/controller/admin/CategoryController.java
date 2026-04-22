package com.nex.nexmart.controller.admin;

import com.nex.nexmart.common.Result;
import com.nex.nexmart.model.dto.CategoryDTO;
import com.nex.nexmart.model.entity.Category;
import com.nex.nexmart.security.SecurityUserDetails;
import com.nex.nexmart.service.intf.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "管理端-商品分类管理")
@RestController("adminCategoryController")
@RequestMapping("/api/admin/category")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'BOSS')")
@Slf4j
public class CategoryController {

    private final CategoryService categoryService;
    @Operation(summary = "查询所有分类")
    @GetMapping("/list")
    public Result<List<Category>> list(@AuthenticationPrincipal SecurityUserDetails userDetails) {
        Integer role = userDetails.getUser().getRole();
		return Result.success(categoryService.selectList(role));
    }

    @Operation(summary = "新增分类")
    @PostMapping
    public Result<Void> add(@Valid @RequestBody CategoryDTO dto) {
        log.info("新增分类：{}", dto);
        categoryService.addCategory(dto);
        return Result.success();
    }

    @Operation(summary = "修改分类")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody CategoryDTO dto) {
        log.info("修改分类：{}", dto);
        categoryService.updateCategory(id, dto);
        return Result.success();
    }

    @Operation(summary = "删除分类")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        log.info("删除分类：{}", id);
        categoryService.removeCategory(id);
        return Result.success();
    }
}
