package com.github.jerrymice.json.schema.listener;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.jerrymice.json.schema.provider.ValidateMessageProvider;
import com.github.jerrymice.json.schema.provider.impl.DefaultValidateMessageProvider;
import com.google.inject.internal.util.Sets;
import com.networknt.schema.ValidationMessage;
import com.networknt.schema.walk.JsonSchemaWalkListener;
import com.networknt.schema.walk.WalkEvent;
import com.networknt.schema.walk.WalkFlow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

import java.util.Set;

public class ErrorMessageRewriteWalkListener implements JsonSchemaWalkListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(ErrorMessageRewriteWalkListener.class);
    private Level loggerLevel = Level.WARN;
    private ValidateMessageProvider validateMessageProvider;

    public ErrorMessageRewriteWalkListener() {
        validateMessageProvider = new DefaultValidateMessageProvider();
    }

    public ErrorMessageRewriteWalkListener(ValidateMessageProvider validateMessageProvider) {
        this.validateMessageProvider = validateMessageProvider;
    }

    public ValidateMessageProvider getValidateMessageProvider() {
        return validateMessageProvider;
    }

    public void setValidateMessageProvider(ValidateMessageProvider validateMessageProvider) {
        this.validateMessageProvider = validateMessageProvider;
    }

    public Level getLoggerLevel() {
        return loggerLevel;
    }

    public void setLoggerLevel(Level loggerLevel) {
        this.loggerLevel = loggerLevel;
    }

    @Override
    public WalkFlow onWalkStart(WalkEvent walkEvent) {
        return WalkFlow.CONTINUE;
    }

    @Override
    public void onWalkEnd(WalkEvent walkEvent, Set<ValidationMessage> validationMessages) {
        if (validationMessages.isEmpty()) {
            return;
        }
        Set<ValidationMessage> rewriteMessageSet = Sets.newHashSet();
        for (ValidationMessage message : validationMessages) {
            ValidationMessage rewrite = rewrite(walkEvent, message);
            rewriteMessageSet.add(rewrite);
        }
        validationMessages.clear();
        validationMessages.addAll(rewriteMessageSet);
    }

    /**
     * 重写验证失败信息，如果重写出现异常，那么返回原始消息
     *
     * @param walkEvent
     * @param message
     * @return
     */
    private ValidationMessage rewrite(WalkEvent walkEvent, ValidationMessage message) {
        try {
            if (!message.getMessage().startsWith("$.")) {
                return message;
            }
            return validateMessageProvider.rewrite(walkEvent, message);
        } catch (Exception e) {
            JsonNode schemaNode = walkEvent.getSchemaNode();
            JsonNode node = walkEvent.getNode();
            switch (loggerLevel) {
                case ERROR:
                    LOGGER.error("重写验证消息失败,schema node:{},data node:{},source message:{},ex:",
                            schemaNode.asText(), node.asText(), message.toString(), e);
                    break;
                case INFO:
                    LOGGER.info("重写验证消息失败,schema node:{},data node:{},source message:{}",
                            schemaNode.asText(), node.asText(), message.toString());
                default:
                    LOGGER.warn("重写验证消息失败,schema node:{},data node:{},source message:{},ex:",
                            schemaNode.asText(), node.asText(), message.toString(), e);
            }
            return message;
        }
    }
}
