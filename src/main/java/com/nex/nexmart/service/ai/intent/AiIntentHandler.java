package com.nex.nexmart.service.ai.intent;

import com.nex.nexmart.common.IntentResult;

public interface AiIntentHandler {

	String intent();

	String handle(IntentResult intentResult, Long userId);
}
