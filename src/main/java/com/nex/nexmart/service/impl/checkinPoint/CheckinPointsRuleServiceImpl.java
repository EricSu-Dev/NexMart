package com.nex.nexmart.service.impl.checkinPoint;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nex.nexmart.exception.BusinessException;
import com.nex.nexmart.model.entity.checkinPoint.CheckinPointsRule;
import com.nex.nexmart.service.intf.checkinPoint.CheckinPointsRuleService;
import com.nex.nexmart.mapper.base.CheckinPointsRuleMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
* @author Eric
* @description 针对表【checkin_points_rule(签到积分规则表)】的数据库操作Service实现
* @createDate 2026-04-16 08:18:30
*/
@Service
public class CheckinPointsRuleServiceImpl extends ServiceImpl<CheckinPointsRuleMapper, CheckinPointsRule> implements CheckinPointsRuleService{
	public Map<Integer, Integer> getRuleMap() {
		return list().stream().collect(
				Collectors.toMap(
						CheckinPointsRule::getConsecutiveDays,
						CheckinPointsRule::getPoints
				)
		);
	}

	@Override
	public void updateRule(Long id, Integer points) {
		if (points < 1) {
			throw new BusinessException("积分不能小于1");
		}
		boolean success = lambdaUpdate()
				.eq(CheckinPointsRule::getId, id)
				.set(CheckinPointsRule::getPoints, points)
				.update();
		if (!success) {
			throw new BusinessException("规则不存在");
		}
	}

	public List<CheckinPointsRule> getRuleList(){
		return lambdaQuery().orderByAsc(CheckinPointsRule::getConsecutiveDays).list();
	}

}




