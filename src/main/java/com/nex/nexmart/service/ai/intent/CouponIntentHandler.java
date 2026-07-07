package com.nex.nexmart.service.ai.intent;

import com.nex.nexmart.common.IntentResult;
import com.nex.nexmart.service.ai.AiContextService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CouponIntentHandler implements AiIntentHandler {

	private final AiContextService aiContextService;

	@Override
	public String intent() {
		return "query_coupon";
	}

	@Override
	public String handle(IntentResult intentResult, Long userId) {
		return aiContextService.queryCoupon(userId);
	}
}
