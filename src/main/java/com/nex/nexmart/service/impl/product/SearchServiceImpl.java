package com.nex.nexmart.service.impl.product;

import com.nex.nexmart.service.intf.product.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {

	private final RedisTemplate<String, String> redisTemplate;
	private static final String HOT_KEY = "NexMart:search:hot";
	private static final int MAX_KEYWORD_LENGTH = 33;

	@Override
	public void recordKeyword(String keyword) {
		keyword = keyword.trim();// 去除首尾空格
		if (keyword.length() > MAX_KEYWORD_LENGTH) return;
		redisTemplate.opsForZSet().incrementScore(HOT_KEY, keyword, 1);
	}

	@Override
	public List<String> getHotKeywords() {
		Set<String> set = redisTemplate.opsForZSet().reverseRange(HOT_KEY, 0, 4);//reverseRange从大到小, range从小到大
		return set != null ? new ArrayList<>(set) : Collections.emptyList();
	}

	// 每天凌晨3点将score衰减10%，并删除score小于1的关键词
	@Scheduled(cron = "0 0 3 * * ?")  // 每天凌晨3点执行
	public void decayHotKeywords() {
		Set<ZSetOperations.TypedTuple<String>> tuples =
				redisTemplate.opsForZSet().rangeWithScores(HOT_KEY, 0, -1);
		if (tuples == null || tuples.isEmpty()) return;

		tuples.forEach(tuple -> {
			double newScore = tuple.getScore() * 0.9;
			if (newScore < 1) {
				// score过低直接删掉，避免ZSet无限膨胀
				redisTemplate.opsForZSet().remove(HOT_KEY, tuple.getValue());
			} else {
				redisTemplate.opsForZSet().add(HOT_KEY, tuple.getValue(), newScore);
			}
		});
	}
}
