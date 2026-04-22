package com.nex.nexmart.controller.admin;

import com.nex.nexmart.common.PageResult;
import com.nex.nexmart.common.Result;
import com.nex.nexmart.model.dto.seckill.BindSeckillItemDTO;
import com.nex.nexmart.model.dto.seckill.SeckillActivityDTO;
import com.nex.nexmart.model.vo.seckill.SeckillActivityVO;
import com.nex.nexmart.service.intf.seckill.SeckillActivityService;
import com.nex.nexmart.service.intf.seckill.SeckillItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "管理端-秒杀活动管理")
@Slf4j
@RestController("AdminSeckillActivityController")
@RequestMapping("/api/admin/seckill/activity")
@RequiredArgsConstructor
public class SeckillActivityController {

	private final SeckillActivityService seckillActivityService;
	private final SeckillItemService seckillItemService;

	@GetMapping("/page")
	@Operation(summary = "分页查询秒杀活动")
	public Result<PageResult<SeckillActivityVO>> page(
			@RequestParam(defaultValue = "1") Integer page,
			@RequestParam(defaultValue = "10") Integer size,
			@RequestParam(required = false) Integer status,
			@RequestParam(required = false) Integer phase,
			@RequestParam(required = false) Integer activityType) {
		log.info("分页查询秒杀活动: page={}, size={}, status={}", page, size, status);
		return Result.success(seckillActivityService.pageActivity(page, size, status,phase,activityType));
	}

	@PostMapping
	@Operation(summary = "创建秒杀活动")
	public Result<Void> create(@RequestBody @Validated SeckillActivityDTO dto) {
		log.info("创建秒杀活动: {}", dto.getName());
		seckillActivityService.createActivity(dto);
		return Result.success();
	}

	@PutMapping("/{id}")
	@Operation(summary = "修改秒杀活动")
	public Result<Void> update(@PathVariable Long id, @RequestBody @Validated SeckillActivityDTO dto) {
		log.info("修改秒杀活动: {}", id);
		seckillActivityService.updateActivity(id, dto);
		return Result.success();
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "删除秒杀活动")
	public Result<Void> delete(@PathVariable Long id) {
		log.info("删除秒杀活动: {}", id);
		seckillActivityService.deleteActivity(id);
		return Result.success();
	}

	@PatchMapping("/{id}/status")
	@Operation(summary = "修改秒杀活动状态")
	public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
		log.info("修改秒杀活动状态: {}", id);
		seckillActivityService.updateStatus(id, status);
		return Result.success();
	}

	@PatchMapping("/bind")
	@Operation(summary = "添加活动秒杀项")
	public Result<Void> bindActivity(@RequestBody @Validated BindSeckillItemDTO dto) {
		seckillItemService.bindActivity(dto);
		return Result.success();
	}

	@PatchMapping("/bind/delete")
	@Operation(summary = "删除活动秒杀项")
	public Result<Void> deleteBind(@RequestBody @Validated BindSeckillItemDTO dto) {
		seckillItemService.deleteBind(dto);
		return Result.success();
	}

	@GetMapping("/activity/name")
	@Operation(summary = "获取秒杀活动名称")
	public Result<Map<Long, String>> listActivityName(Integer activityType){
		log.info("获取秒杀活动名称: {}", activityType);
		return Result.success(seckillActivityService.getActivityNameMap(activityType));
	}
}
