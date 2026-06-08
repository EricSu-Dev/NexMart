package com.nex.nexmart.service.impl.seckill;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nex.nexmart.common.PageResult;
import com.nex.nexmart.common.constant.RedisSeckillConstants;
import com.nex.nexmart.exception.BusinessException;
import com.nex.nexmart.model.dto.seckill.SeckillActivityDTO;
import com.nex.nexmart.model.entity.seckill.SeckillActivity;
import com.nex.nexmart.model.entity.seckill.SeckillItem;
import com.nex.nexmart.model.vo.seckill.SeckillActivityVO;
import com.nex.nexmart.service.intf.seckill.SeckillActivityService;
import com.nex.nexmart.mapper.base.SeckillActivityMapper;
import com.nex.nexmart.service.intf.seckill.SeckillItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author Eric
 * 针对表【seckill_activity(秒杀活动表)】的数据库操作Service实现
 * 2026-04-13 16:10:34
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SeckillActivityServiceImpl extends ServiceImpl<SeckillActivityMapper, SeckillActivity> implements SeckillActivityService {

	private final SeckillItemService seckillItemService;
	private final RedisTemplate<String, String> redisTemplate;
	@Override
	public PageResult<SeckillActivityVO> pageActivity(Integer current, Integer size, Integer status, Integer phase, Integer activityType) {
		LocalDateTime now = LocalDateTime.now();
		Page<SeckillActivity> page = lambdaQuery()
				.eq(status != null, SeckillActivity::getStatus, status)
				.eq(activityType != null, SeckillActivity::getActivityType, activityType)
				.gt(phase != null && phase == 1, SeckillActivity::getStartTime, now)//未开始
				.lt(phase != null && phase == 2, SeckillActivity::getStartTime, now)//进行中
				.gt(phase != null && phase == 2, SeckillActivity::getEndTime, now)//进行中
				.lt(phase != null && phase == 3, SeckillActivity::getEndTime, now)//已结束
				.orderByDesc(SeckillActivity::getCreatedAt)
				.page(new Page<>(current, size));

		List<SeckillActivityVO> voList = page.getRecords().stream().map(item -> {
			SeckillActivityVO vo = new SeckillActivityVO();
			BeanUtils.copyProperties(item, vo);
			vo.setItemCount(seckillItemService.lambdaQuery()
					.eq(SeckillItem::getActivityId, item.getId())
					.count());
			vo.setPhase(calculatePhase(item.getStartTime(), item.getEndTime()));
			return vo;
		}).collect(Collectors.toList());

		return PageResult.of(voList, page.getTotal(), current, size);
	}

	@Override
	public void createActivity(SeckillActivityDTO dto) {
		if (!dto.getEndTime().isAfter(dto.getStartTime())) {
			throw new BusinessException("结束时间必须晚于开始时间");
		}
		SeckillActivity activity = new SeckillActivity();
		BeanUtils.copyProperties(dto, activity);
		activity.setStatus(1);//默认启用
		save(activity);
		//Cache Aside模式，写操作先更新DB再删缓存
		redisTemplate.delete(RedisSeckillConstants.SECKILL_ACTIVITY+dto.getActivityType());
	}

	@Override
	public void updateActivity(Long id, SeckillActivityDTO dto) {
		SeckillActivity activity = getById(id);
		if (activity == null) throw new BusinessException("活动不存在");
		LocalDateTime now = LocalDateTime.now();
		boolean isOngoing = !now.isBefore(activity.getStartTime())
				&& !now.isAfter(activity.getEndTime());
		if (activity.getStatus() != 2 && isOngoing) throw new BusinessException("进行中且已启用的活动不能编辑");
		if (!dto.getEndTime().isAfter(dto.getStartTime())) {
			throw new BusinessException("结束时间必须晚于开始时间");
		}
		BeanUtils.copyProperties(dto, activity);
		updateById(activity);
		redisTemplate.delete(RedisSeckillConstants.SECKILL_ACTIVITY+dto.getActivityType());
	}

	@Override
	public void deleteActivity(Long id) {
		SeckillActivity activity = getById(id);
		if (activity == null) throw new BusinessException("活动不存在");
		LocalDateTime now = LocalDateTime.now();
		boolean isOngoing = !now.isBefore(activity.getStartTime())
				&& !now.isAfter(activity.getEndTime());
		if (activity.getStatus() != 2 && isOngoing) throw new BusinessException("进行中且已启用的活动不能删除");
		removeById(id);
		redisTemplate.delete(RedisSeckillConstants.SECKILL_ACTIVITY+activity.getActivityType());
	}

	@Override
	public void updateStatus(Long id, Integer status) {
		SeckillActivity activity = getById(id);
		if (activity == null) throw new BusinessException("活动不存在");
		activity.setStatus(status);
		updateById(activity);
		redisTemplate.delete(RedisSeckillConstants.SECKILL_ACTIVITY+activity.getActivityType());
	}

	public Map<Long,String> getActivityNameMap(Integer activityType) {
		Map<Long, String> map = new HashMap<>();
		List<SeckillActivity> list = lambdaQuery()
				.eq(SeckillActivity::getActivityType, activityType)
				.select(SeckillActivity::getId, SeckillActivity::getName)
				.list();
		list.forEach(item -> map.put(item.getId(), item.getName()));
		return map;
	}

	//-----------------------------用户端-----------------------------------
	@SuppressWarnings("BusyWait")
	@Override
	public List<SeckillActivityVO> listActivity(Integer activityType) {
		String cacheKey = RedisSeckillConstants.SECKILL_ACTIVITY + activityType;
		String lockKey = RedisSeckillConstants.SECKILL_ACTIVITY_LOCK + activityType;
		String requestId = UUID.randomUUID().toString();  // 每个请求唯一标识

		while (true) {
			// 1. 查缓存
			String cached = redisTemplate.opsForValue().get(cacheKey);
			if (cached != null) {
				if ("[]".equals(cached)) return Collections.emptyList();
				return toVO(JSON.parseArray(cached, SeckillActivity.class));
			}

			// 2. 抢互斥锁
			Boolean locked = redisTemplate.opsForValue()
					.setIfAbsent(lockKey, requestId, 10, TimeUnit.SECONDS);

			if (!Boolean.TRUE.equals(locked)) {
				// 抢锁失败，等待后继续循环
				try { Thread.sleep(50); } catch (InterruptedException ignored) {}
				continue;
			}

			try {
				// 3. 双重检查
				cached = redisTemplate.opsForValue().get(cacheKey);
				if (cached != null) {
					if ("[]".equals(cached)) return Collections.emptyList();
					return toVO(JSON.parseArray(cached, SeckillActivity.class));
				}

				// 4. 查DB
				LocalDateTime now = LocalDateTime.now();
				LocalDateTime oneWeekLater = now.plusDays(7);
				LocalDateTime sevenDaysAgo = now.minusDays(7);
				List<SeckillActivity> activities = lambdaQuery()
						.eq(SeckillActivity::getStatus, 1)
						.eq(SeckillActivity::getActivityType, activityType)
						.apply("(start_time <= {0} AND end_time >= {0})" +
										" OR (start_time > {0} AND start_time <= {1})" +
										" OR (end_time < {0} AND end_time >= {2})",
								now, oneWeekLater, sevenDaysAgo)
						.orderByAsc(SeckillActivity::getEndTime)
						.list();

				// 5. 写缓存, 随机过期时间，防止雪崩,返回空值, 防止缓存穿透
				if (activities == null || activities.isEmpty()) {
					redisTemplate.opsForValue().set(cacheKey, "[]", 2, TimeUnit.MINUTES); // 空值TTL设短一点
				} else {
					redisTemplate.opsForValue().set(
							cacheKey,
							JSON.toJSONString(activities),
							5 + new Random().nextInt(3),
							TimeUnit.MINUTES
					);
				}

				return toVO(activities);
			} finally {
				// 安全释放锁：只有自己持有的才删除(如果线程执行时间超过10秒,锁过期,其他线程抢到锁,直接用delete会删除其他线程的锁)
				redisTemplate.execute(new DefaultRedisScript<>(RedisSeckillConstants.RELEASE_LOCK_SCRIPT, Long.class),
						Collections.singletonList(lockKey), requestId); //singletonList:只包含一个元素的不可变List
			}
		}
	}

	// 抽取转VO逻辑
	private List<SeckillActivityVO> toVO(List<SeckillActivity> activities) {
		return activities.stream().map(item -> {
			SeckillActivityVO vo = new SeckillActivityVO();
			BeanUtils.copyProperties(item, vo);
			vo.setPhase(calculatePhase(item.getStartTime(), item.getEndTime()));
			return vo;
		}).collect(Collectors.toList());
	}

	private Integer calculatePhase(LocalDateTime start, LocalDateTime end) {
		LocalDateTime now = LocalDateTime.now();
		if (now.isBefore(start)) return 1;
		if (now.isAfter(end)) return 3;
		return 2;
	}

}



