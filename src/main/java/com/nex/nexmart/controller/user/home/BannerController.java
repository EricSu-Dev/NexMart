package com.nex.nexmart.controller.user.home;

import com.nex.nexmart.common.Result;
import com.nex.nexmart.model.vo.home.BannerVO;
import com.nex.nexmart.service.intf.home.BannerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "用户端-轮播图接口")
@RestController("userBannerController")
@RequestMapping("/api/user/banner")
@RequiredArgsConstructor
@Slf4j
public class BannerController {
	private final BannerService bannerService;
	@GetMapping
	@Operation(summary = "获取轮播图")
	public Result<List<BannerVO>> list() {
		return Result.success(bannerService.getActiveBanners());
	}
}
