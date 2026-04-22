package com.nex.nexmart.controller.user.seckill;

import com.nex.nexmart.common.Result;
import com.nex.nexmart.model.vo.seckill.SeckillActivityVO;
import com.nex.nexmart.service.intf.seckill.SeckillActivityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@Tag(name = "用户端-秒杀活动")
@RestController("UserSeckillActivityController")
@RequestMapping("/api/user/seckill/activity")
@RequiredArgsConstructor
public class SeckillActivityController {

	private final SeckillActivityService seckillActivityService;

	@GetMapping("/list")
	@Operation(summary = "获取秒杀活动列表")
	public Result<List<SeckillActivityVO>> list(
			@RequestParam(required = false) Integer activityType) {
		log.info("获取秒杀活动列表");
		return Result.success(seckillActivityService.listActivity(activityType));
	}

}