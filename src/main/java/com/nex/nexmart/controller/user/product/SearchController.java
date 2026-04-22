package com.nex.nexmart.controller.user.product;

import com.nex.nexmart.common.Result;
import com.nex.nexmart.service.intf.product.SearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "用户端-搜索接口")
@Slf4j
@RestController
@RequestMapping("/api/user/search")
@RequiredArgsConstructor
public class SearchController {

	private final SearchService searchService;

	@Operation(summary = "获取热门搜索词")
	@GetMapping("/hot")
	public Result<List<String>> getHotKeywords() {
		return Result.success(searchService.getHotKeywords());
	}
}
