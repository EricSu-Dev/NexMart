package com.nex.nexmart.controller.user.product;

import com.nex.nexmart.common.constant.UserRoleConstants;
import com.nex.nexmart.common.Result;
import com.nex.nexmart.model.entity.Category;
import com.nex.nexmart.security.SecurityUserDetails;
import com.nex.nexmart.service.intf.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "用户端-商品分类接口")
@RestController("userCategoryController")
@RequestMapping("/api/user/category")
@RequiredArgsConstructor
@Slf4j
public class CategoryController {

    private final CategoryService categoryService;

    @Operation(summary = "查询所有分类（公开）")
    @GetMapping("/list")
    public Result<List<Category>> list(@AuthenticationPrincipal SecurityUserDetails userDetails) {
        log.info("查询所有分类");
		Integer role = userDetails == null ? UserRoleConstants.ROLE_USER : userDetails.getUser().getRole();
        return Result.success(categoryService.selectList(role));
    }
}
