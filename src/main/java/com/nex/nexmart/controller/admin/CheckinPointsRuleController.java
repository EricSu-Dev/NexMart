package com.nex.nexmart.controller.admin;

import com.nex.nexmart.common.Result;
import com.nex.nexmart.model.entity.checkinPoint.CheckinPointsRule;
import com.nex.nexmart.service.intf.checkinPoint.CheckinPointsRuleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@Tag(name = "管理端-签到积分规则")
@RestController
@RequestMapping("/api/admin/checkin/rules")
@RequiredArgsConstructor
public class CheckinPointsRuleController {

	private final CheckinPointsRuleService checkinPointsRuleService;

	@GetMapping
	@Operation(summary = "查询所有签到积分规则")
	public Result<List<CheckinPointsRule>> list() {
		log.info("查询所有签到积分规则");
		return Result.success(checkinPointsRuleService.getRuleList());
	}

	@PutMapping("/{id}")
	@Operation(summary = "修改签到积分规则")
	public Result<Void> update(@PathVariable Long id, @RequestParam Integer points) {
		log.info("修改签到积分规则");
		checkinPointsRuleService.updateRule(id, points);
		return Result.success();
	}
}
