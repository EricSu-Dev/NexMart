package com.nex.nexmart.controller.user.home;

import com.nex.nexmart.common.Result;
import com.nex.nexmart.model.vo.home.HomeVO;
import com.nex.nexmart.service.intf.home.HomeSectionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "用户端-首页模块接口")
@RestController
@RequestMapping("/api/user/home/section")
@RequiredArgsConstructor
@Slf4j
public class UserHomeSectionController {
	private final HomeSectionService homeSectionService;
	@GetMapping
	@Operation(summary = "获取首页模块数据")
	public Result<HomeVO> getHome() {
		return Result.success(homeSectionService.getHome());
	}
}
