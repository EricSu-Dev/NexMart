package com.nex.nexmart.service.ai.intent;

import com.nex.nexmart.common.IntentResult;
import com.nex.nexmart.service.ai.AiContextService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PromotionIntentHandler implements AiIntentHandler {

	private final AiContextService aiContextService;

	@Override
	public String intent() {
		return "query_promotion";
	}

	@Override
	public String handle(IntentResult intentResult, Long userId) {
		return aiContextService.queryPromotion();
	}
}
