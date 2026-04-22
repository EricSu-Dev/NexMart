package com.nex.nexmart.service.impl.checkinPoint;

import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nex.nexmart.common.PageResult;
import com.nex.nexmart.mapper.base.UserPointsLogMapper;
import com.nex.nexmart.model.entity.checkinPoint.UserPoints;
import com.nex.nexmart.model.entity.checkinPoint.UserPointsLog;
import com.nex.nexmart.model.vo.checkinPoint.PointsLogVO;
import com.nex.nexmart.service.intf.checkinPoint.UserPointsService;
import com.nex.nexmart.mapper.UserPointsMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
* @author Eric
*  针对表【user_points(用户积分账户表)】的数据库操作Service实现
*  2026-04-11 18:06:52
*/
@Service
@RequiredArgsConstructor
public class UserPointsServiceImpl extends ServiceImpl<UserPointsMapper, UserPoints> implements UserPointsService{
	private final UserPointsLogMapper userPointsLogMapper;
	private final UserPointsMapper userPointsMapper;

	@Override
	public int addPoints(Long userId, int delta) {
		// INSERT ... ON DUPLICATE KEY UPDATE，先尝试插入新账户，已存在则累加
		userPointsMapper.upsertPoints(userId, delta);
		return lambdaQuery().eq(UserPoints::getUserId, userId).one().getTotalPoints();
	}

	@Override
	public void writeLog(Long userId, int changeType, int delta,
	                     int balance, String remark, Long refId) {
		UserPointsLog log = new UserPointsLog();
		log.setUserId(userId);
		log.setChangeType(changeType);
		log.setPointsDelta(delta);
		log.setBalance(balance);
		log.setRemark(remark);
		log.setRefId(refId);
		log.setCreatedAt(LocalDateTime.now());
		userPointsLogMapper.insert(log);
	}

	@Override
	public PageResult<PointsLogVO> getLogs(Long userId, long current, long size) {
		Page<UserPointsLog> page = new Page<>(current, size);
		Page<UserPointsLog> result = new LambdaQueryChainWrapper<>(userPointsLogMapper)
				.eq(UserPointsLog::getUserId, userId)
				.orderByDesc(UserPointsLog::getCreatedAt)
				.page(page);

		List<PointsLogVO> voList = result.getRecords().stream().map(log -> {
			PointsLogVO vo = new PointsLogVO();
			vo.setChangeType(log.getChangeType());
			vo.setPointsDelta(log.getPointsDelta());
			vo.setBalance(log.getBalance());
			vo.setRemark(log.getRemark());
			vo.setCreatedAt(log.getCreatedAt());
			return vo;
		}).collect(Collectors.toList());

		return PageResult.of(voList, result.getTotal(), result.getCurrent(), result.getSize());
	}
}




