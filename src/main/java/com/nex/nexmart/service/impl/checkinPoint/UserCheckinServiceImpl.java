package com.nex.nexmart.service.impl.checkinPoint;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nex.nexmart.common.constant.PointsChangeTypeConstant;
import com.nex.nexmart.exception.BusinessException;
import com.nex.nexmart.model.entity.checkinPoint.UserCheckin;
import com.nex.nexmart.model.entity.checkinPoint.UserPoints;
import com.nex.nexmart.model.vo.checkinPoint.CheckinResultVO;
import com.nex.nexmart.model.vo.checkinPoint.CheckinStatusVO;
import com.nex.nexmart.service.intf.checkinPoint.CheckinPointsRuleService;
import com.nex.nexmart.service.intf.checkinPoint.UserCheckinService;
import com.nex.nexmart.mapper.base.UserCheckinMapper;
import com.nex.nexmart.service.intf.checkinPoint.UserPointsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.BitFieldSubCommands;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;


/**
* @author Eric
*  针对表【user_checkin(用户签到记录表)】的数据库操作Service实现
*  2026-04-11 18:06:08
*/
@Slf4j
@Service
@RequiredArgsConstructor
public class UserCheckinServiceImpl extends ServiceImpl<UserCheckinMapper, UserCheckin> implements UserCheckinService{
	private final UserPointsService userPointsService;
	private final CheckinPointsRuleService checkinPointsRuleService;
	private final StringRedisTemplate stringRedisTemplate;
	private final RedisTemplate<String, byte[]> redisTemplate;

	@Override
	@Transactional
	public CheckinResultVO checkin(Long userId) {
		LocalDate today = LocalDate.now();

		// 1. 检查今日是否已签到
		Long count = lambdaQuery()
				.eq(UserCheckin::getUserId, userId)
				.eq(UserCheckin::getCheckinDate, today)
				.count();
		if (count > 0) {
			throw new BusinessException("今日已签到");
		}

		// 2. 计算连续签到天数
		int consecutiveDays = updateRedisAndGetConsecutiveDays(userId, today);

		// 3. 计算本次积分和奖励说明
		int pointsEarned = calcPoints(consecutiveDays);
		String remark = buildRemark(consecutiveDays);

		// 4. 插入签到记录
		UserCheckin checkin = new UserCheckin();
		checkin.setUserId(userId);
		checkin.setCheckinDate(today);
		checkin.setPointsEarned(pointsEarned);
		checkin.setConsecutiveDays(consecutiveDays);
		checkin.setCreatedAt(LocalDateTime.now());
		save(checkin);

		// 5. 更新积分余额（UPSERT）并获取最新余额
		int newBalance = userPointsService.addPoints(userId, pointsEarned);

		// 6. 写积分流水
		userPointsService.writeLog(userId, PointsChangeTypeConstant.CHECKIN,
				pointsEarned, newBalance, remark, null);

		// 7. 组装返回
		CheckinResultVO vo = new CheckinResultVO();
		vo.setPointsEarned(pointsEarned);
		vo.setConsecutiveDays(consecutiveDays);
		vo.setTotalPoints(newBalance);
		vo.setBonusRemark(remark);
		return vo;
	}

	@Override
	public CheckinStatusVO getStatus(Long userId, LocalDate target) {

		// 从Redis读本月签到情况
		List<Integer> checkedDays = getCheckedDaysFromRedis(userId, target);

		// Redis没数据时降级查DB（首次使用或key过期）
		if (checkedDays.isEmpty()) {
			checkedDays = getCheckedDaysFromDB(userId, target);
		}

		LocalDate today = LocalDate.now();

		// 今日是否签到
		boolean todayChecked = target.getMonth().equals(today.getMonth())
				&& checkedDays.contains(today.getDayOfMonth());

		// 当前连续天数
		int consecutiveDays = getConsecutiveDays(userId, today);

		// 当前积分余额
		UserPoints userPoints = userPointsService.lambdaQuery().eq(UserPoints::getUserId, userId).one();
		int totalPoints = userPoints == null ? 0 : userPoints.getTotalPoints();

		CheckinStatusVO vo = new CheckinStatusVO();
		vo.setCheckedDays(checkedDays);
		vo.setConsecutiveDays(consecutiveDays);
		vo.setTodayChecked(todayChecked);
		vo.setTotalPoints(totalPoints);
		return vo;
	}

	private int getConsecutiveDays(Long userId, LocalDate baseDate) {
		String consecutiveKey = "NexMart:checkin:consecutive:" + userId;
		String lastKey = "NexMart:checkin:last:" + userId;
		try {
			String lastDate = stringRedisTemplate.opsForValue().get(lastKey);
			String val = stringRedisTemplate.opsForValue().get(consecutiveKey);

			if (lastDate == null || val == null) {
				return calcConsecutiveDaysFromDB(userId, baseDate);
			}

			// 判断是否断签,更新Redis的连续签到天数
			long daysBetween = ChronoUnit.DAYS.between(LocalDate.parse(lastDate), baseDate);
			if (daysBetween > 1) return 0;

			return Integer.parseInt(val);
		} catch (Exception e) {
			return calcConsecutiveDaysFromDB(userId, baseDate);
		}
	}
	private int calcConsecutiveDaysFromDB(Long userId, LocalDate baseDate) {
		UserCheckin last = lambdaQuery()
				.eq(UserCheckin::getUserId, userId)
				.orderByDesc(UserCheckin::getCheckinDate)
				.last("LIMIT 1")
				.one();

		if (last == null) return 0;

		long daysBetween = ChronoUnit.DAYS.between(last.getCheckinDate(), baseDate);
		if (daysBetween > 1) return 0; // 断签了

		// 回填Redis
		String lastKey = "NexMart:checkin:last:" + userId;
		String consecutiveKey = "NexMart:checkin:consecutive:" + userId;
		stringRedisTemplate.opsForValue().set(lastKey, last.getCheckinDate().toString());
		stringRedisTemplate.opsForValue().set(consecutiveKey, String.valueOf(daysBetween > 1 ? 0 : last.getConsecutiveDays()));

		return daysBetween > 1 ? 0 : last.getConsecutiveDays();
	}

	// 签到用，更新计数器
	private int updateRedisAndGetConsecutiveDays(Long userId, LocalDate today) {
		String consecutiveKey = "NexMart:checkin:consecutive:" + userId;
		String lastKey = "NexMart:checkin:last:" + userId;
		String bitKey = "NexMart:checkin:" + userId + ":" + today.format(DateTimeFormatter.ofPattern("yyyy-MM"));

		// 写Bitmap
		stringRedisTemplate.opsForValue().setBit(bitKey, today.getDayOfMonth()-1, true);

		// 获取lastDate用于判断是否断签
		String lastDate = stringRedisTemplate.opsForValue().get(lastKey);

		// lastKey不存在，先从DB回填
		if (lastDate == null) {
			UserCheckin last = lambdaQuery()
					.eq(UserCheckin::getUserId, userId)
					.orderByDesc(UserCheckin::getCheckinDate)
					.last("LIMIT 1")
					.one();
			if (last != null) {
				lastDate = last.getCheckinDate().toString();
				long daysBetween = ChronoUnit.DAYS.between(last.getCheckinDate(), today);
				int prevConsecutive = daysBetween > 1 ? 0 : last.getConsecutiveDays();
				stringRedisTemplate.opsForValue().set(lastKey, lastDate);
				stringRedisTemplate.opsForValue().set(consecutiveKey, String.valueOf(prevConsecutive));
			}
		}

		int days;
		if (lastDate != null && LocalDate.parse(lastDate).plusDays(1).equals(today)) {
			Long newVal = stringRedisTemplate.opsForValue().increment(consecutiveKey);
			days = newVal == null ? 1 : newVal.intValue();
		} else {
			days = 1;
			stringRedisTemplate.opsForValue().set(consecutiveKey, "1");
		}

		stringRedisTemplate.opsForValue().set(lastKey, today.toString());
		return days;
	}

	private List<Integer> getCheckedDaysFromRedis(Long userId, LocalDate month) {
		String key = "NexMart:checkin:" + userId + ":" + month.format(DateTimeFormatter.ofPattern("yyyy-MM"));
		int daysInMonth = month.lengthOfMonth();

		BitFieldSubCommands commands = BitFieldSubCommands.create()
				.get(BitFieldSubCommands.BitFieldType.unsigned(daysInMonth))
				.valueAt(0);
		List<Long> result = stringRedisTemplate.opsForValue().bitField(key, commands);

		if (result == null || result.isEmpty() || result.getFirst() == null) {
			return Collections.emptyList();
		}

		// 把数字转成签到日列表
		long bits = result.getFirst();
		List<Integer> checkedDays = new ArrayList<>();
		for (int i = daysInMonth; i >= 1; i--) {
			if ((bits & 1) == 1) checkedDays.add(i);
			bits >>= 1;
		}
		return checkedDays;
	}

	private List<Integer> getCheckedDaysFromDB(Long userId, LocalDate target) {
		//先从DB查询本月签到日期
		List<UserCheckin> records = lambdaQuery()
				.eq(UserCheckin::getUserId, userId)
				.between(UserCheckin::getCheckinDate,
						target.withDayOfMonth(1),
						target.withDayOfMonth(target.lengthOfMonth()))
				.list();

		// 回填 last 和 consecutive
		String lastKey = "NexMart:checkin:last:" + userId;
		String consecutiveKey = "NexMart:checkin:consecutive:" + userId;

		boolean hasLast = stringRedisTemplate.hasKey(lastKey);
		boolean hasConsecutive = stringRedisTemplate.hasKey(consecutiveKey);

		if (!hasLast || !hasConsecutive) {
			UserCheckin last = lambdaQuery()
					.eq(UserCheckin::getUserId, userId)
					.orderByDesc(UserCheckin::getCheckinDate)
					.last("LIMIT 1")
					.one();

			if (last != null) {
				long daysBetween = ChronoUnit.DAYS.between(last.getCheckinDate(), LocalDate.now());
				stringRedisTemplate.opsForValue().set(lastKey, last.getCheckinDate().toString());
				stringRedisTemplate.opsForValue().set(consecutiveKey,
						String.valueOf(daysBetween > 1 ? 0 : last.getConsecutiveDays()));
			}
		}

		if (records.isEmpty()) return Collections.emptyList();

		//Redis 的 BITPOS/GETBIT 命令把每个字节的最高位（bit 7）视为该字节的第 0 个偏移
		// 回填bitmap
		String bitKey = "NexMart:checkin:" + userId + ":" + target.format(DateTimeFormatter.ofPattern("yyyy-MM"));
		// 构建一个 4 字节（32位）或 8 字节（64位）的 bitmap
		byte[] bitmap = new byte[4];   // 够用一个月（31天）

		for (UserCheckin r : records) {
			int day = r.getCheckinDate().getDayOfMonth() - 1; // 0~30
			int byteIndex = day / 8;
			int bitIndex = day % 8;
			bitmap[byteIndex] = (byte) (bitmap[byteIndex] | (1 << (7 - bitIndex)));//(bitmap[byteIndex] | (1 << bitIndex))返回的是int,要转成byte
		}

		// 3. 一次性写入
		redisTemplate.opsForValue().set(bitKey, bitmap);
		// 设置过期时间,月初过期
		long daysUntilEnd = target.lengthOfMonth() - LocalDate.now().getDayOfMonth() + 1;
		stringRedisTemplate.expire(bitKey, daysUntilEnd + 2, TimeUnit.DAYS);

		return records.stream()
				.map(r -> r.getCheckinDate().getDayOfMonth())
				.toList();
	}

	private int calcPoints(int consecutiveDays) {
		// key=节点天数, value=积分
		Map<Integer, Integer> ruleMap = checkinPointsRuleService.getRuleMap();
		// 优先匹配节点，没有则取普通签到积分
		return ruleMap.getOrDefault(consecutiveDays,
				ruleMap.getOrDefault(0, 10));
	}

	private String buildRemark(int consecutiveDays) {
		if (consecutiveDays == 30) return "连续签到30天，积分翻倍！";
		if (consecutiveDays == 15) return "连续签到15天，额外奖励！";
		if (consecutiveDays == 7)  return "连续签到7天，额外奖励！";
		if (consecutiveDays == 3)  return "连续签到3天，额外奖励！";
		if (consecutiveDays == 90) return "连续签到90天，额外奖励！";
		if (consecutiveDays == 180) return "连续签到180天，额外奖励！";
		if (consecutiveDays == 365) return "连续签到365天，额外奖励！";
		return "普通签到";
	}
}




