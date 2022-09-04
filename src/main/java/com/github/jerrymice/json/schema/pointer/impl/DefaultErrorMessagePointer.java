package com.github.jerrymice.json.schema.pointer.impl;

import com.github.jerrymice.json.schema.pointer.ErrorMessagePointer;
import com.github.jerrymice.json.schema.pointer.PointFactor;
import com.networknt.schema.ValidationMessage;
import com.networknt.schema.ValidatorTypeCode;

public class DefaultErrorMessagePointer implements ErrorMessagePointer {
    private static final String ERROR_REQUIRED_KEY_WORD = "/error/required";

    @Override
    public boolean isSupport(PointFactor pointFactor) {
        return pointFactor.getType().equals(ValidatorTypeCode.REQUIRED.getValue());
    }

    @Override
    public String createPointer(ValidationMessage validationMessage) {
        if (validationMessage.getSchemaPath().equals("#/required")) {
            return ERROR_REQUIRED_KEY_WORD + "/" + validationMessage.getArguments()[0];
        } else {
            String nodeAt = validationMessage.getSchemaPath()
                    .replace("#/", "")
                    .replace("/required", "");
            return ERROR_REQUIRED_KEY_WORD + "/" + nodeAt + validationMessage.getArguments()[0];
        }
    }
}
