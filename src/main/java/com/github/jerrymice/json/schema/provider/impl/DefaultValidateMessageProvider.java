package com.github.jerrymice.json.schema.provider.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.jerrymice.json.schema.provider.ValidateMessageProvider;
import com.networknt.schema.ValidationMessage;
import com.networknt.schema.walk.WalkEvent;

import java.text.MessageFormat;
import java.util.Optional;

public class DefaultValidateMessageProvider implements ValidateMessageProvider {

    @Override
    public ValidationMessage rewrite(WalkEvent walkEvent, ValidationMessage message) {
        JsonNode schemaNode = walkEvent.getSchemaNode();
        //如果没有自定义的error信息
        JsonNode error = schemaNode.at("/error" + "/" + message.getType());
        if (error.isEmpty()) {
            return message;
        }
        Optional<String> code = Optional.ofNullable(error.get("code")).map(JsonNode::asText);
        Optional<String> messageText = Optional.ofNullable(error.get("message")).map(JsonNode::asText);
        ValidationMessage rewriteMessage = new ValidationMessage.Builder()
                .type(message.getType())
                .path(message.getPath())
                .code(code.orElse(message.getCode()))
                .arguments(message.getArguments())
                .details(message.getDetails())
                .schemaPath(message.getSchemaPath())
                .customMessage(messageText.orElse(message.getMessage()))
                .format(new MessageFormat("")).build();
        return rewriteMessage;
    }
}
