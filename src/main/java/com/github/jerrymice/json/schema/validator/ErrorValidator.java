package com.github.jerrymice.json.schema.validator;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.internal.util.Sets;
import com.networknt.schema.JsonValidator;
import com.networknt.schema.ValidationMessage;

import java.util.Set;

public class ErrorValidator implements JsonValidator {

    @Override
    public Set<ValidationMessage> validate(JsonNode rootNode) {
        return Sets.newHashSet();
    }

    @Override
    public Set<ValidationMessage> validate(JsonNode node, JsonNode rootNode, String at) {
        return Sets.newHashSet();
    }

    @Override
    public Set<ValidationMessage> walk(JsonNode node, JsonNode rootNode, String at, boolean shouldValidateSchema) {
        return Sets.newHashSet();
    }
}
