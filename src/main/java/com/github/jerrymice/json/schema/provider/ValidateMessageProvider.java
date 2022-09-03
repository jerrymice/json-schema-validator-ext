package com.github.jerrymice.json.schema.provider;

import com.networknt.schema.ValidationMessage;
import com.networknt.schema.walk.WalkEvent;


public interface ValidateMessageProvider {
    /**
     * @param walkEvent
     * @param source
     * @return
     */
    ValidationMessage rewrite(WalkEvent walkEvent, ValidationMessage source);
}
