package com.nex.nexmart.service.ai;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.nex.nexmart.mapper.base.AiKnowledgeMapper;
import com.nex.nexmart.model.entity.ai.AiKnowledge;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AiKnowledgeService {

	private static final int TOP_K = 3;

	private final AiKnowledgeMapper aiKnowledgeMapper;

	public String retrieveKnowledge(String query) {
		if (!StringUtils.hasText(query)) {
			return "";
		}
		List<AiKnowledge> knowledgeList = aiKnowledgeMapper.selectList(
				new LambdaQueryWrapper<AiKnowledge>()
						.eq(AiKnowledge::getStatus, 1)
						.orderByDesc(AiKnowledge::getUpdatedAt)
		);
		if (knowledgeList.isEmpty()) {
			return "";
		}
		List<ScoredKnowledge> matched = knowledgeList.stream()
				.map(knowledge -> new ScoredKnowledge(knowledge, score(knowledge, query)))
				.filter(item -> item.score() > 0)
				.sorted(Comparator.comparingInt(ScoredKnowledge::score).reversed())
				.limit(TOP_K)
				.toList();
		if (matched.isEmpty()) {
			return "";
		}
		return matched.stream()
				.map(item -> "- " + item.knowledge().getTitle() + "：" + item.knowledge().getContent())
				.collect(Collectors.joining("\n"));
	}

	private int score(AiKnowledge knowledge, String query) {
		String normalizedQuery = query.toLowerCase();
		String normalizedContent = String.join(" ",
				nonNull(knowledge.getTitle()),
				nonNull(knowledge.getCategory()),
				nonNull(knowledge.getContent()),
				nonNull(knowledge.getTags())
		).toLowerCase();
		int score = 0;
		for (String tag : splitTags(knowledge.getTags())) {
			if (StringUtils.hasText(tag) && normalizedQuery.contains(tag.toLowerCase())) {
				score += 10;
			}
		}
		for (int i = 0; i < normalizedQuery.length(); i++) {
			String token = String.valueOf(normalizedQuery.charAt(i));
			if (StringUtils.hasText(token) && normalizedContent.contains(token)) {
				score++;
			}
		}
		return score;
	}

	private List<String> splitTags(String tags) {
		if (!StringUtils.hasText(tags)) {
			return List.of();
		}
		return Arrays.stream(tags.split("[,，]"))
				.map(String::trim)
				.filter(StringUtils::hasText)
				.toList();
	}

	private String nonNull(String value) {
		return value == null ? "" : value;
	}

	private record ScoredKnowledge(AiKnowledge knowledge, int score) {
	}
}
