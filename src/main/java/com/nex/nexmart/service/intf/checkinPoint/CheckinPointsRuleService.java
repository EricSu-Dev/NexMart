package com.nex.nexmart.service.intf.checkinPoint;

import com.nex.nexmart.model.entity.checkinPoint.CheckinPointsRule;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
* @author Eric
*  针对表【checkin_points_rule(签到积分规则表)】的数据库操作Service
*  2026-04-16 08:18:30
*/
public interface CheckinPointsRuleService extends IService<CheckinPointsRule> {
	Map<Integer, Integer> getRuleMap();
	void updateRule(Long id, Integer points);
	List<CheckinPointsRule> getRuleList();
}
