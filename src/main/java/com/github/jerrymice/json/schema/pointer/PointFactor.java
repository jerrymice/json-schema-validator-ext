package com.github.jerrymice.json.schema.pointer;

import com.networknt.schema.ValidationMessage;

import java.util.Objects;

/**
 *
 */
public class PointFactor {
    private ValidationMessage message;

    public PointFactor(ValidationMessage message) {
        this.message = message;
    }

    public String getCode() {
        return message.getCode();
    }

    public String getPath() {
        return message.getPath();
    }

    public String getSchemaPath() {
        return message.getSchemaPath();
    }

    public String getType() {
        return message.getType();
    }
}
