package com.github.jerrymice.json.schema.provider.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.jerrymice.json.schema.pointer.ErrorMessagePointer;
import com.github.jerrymice.json.schema.pointer.PointFactor;
import com.github.jerrymice.json.schema.pointer.impl.DefaultErrorMessagePointer;
import com.github.jerrymice.json.schema.provider.ValidateMessageProvider;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.ValidationMessage;
import com.networknt.schema.walk.WalkEvent;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DefaultValidateMessageProvider implements ValidateMessageProvider {
    private static final String ERROR_CODE_KEY = "code";
    private static final String ERROR_MESSAGE_KEY = "message";
    private static final String ERROR_KEY = "error";
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultValidateMessageProvider.class);
    protected List<ErrorMessagePointer> errorMessagePointerList = new ArrayList<>();

    public DefaultValidateMessageProvider() {
        errorMessagePointerList.add(new DefaultErrorMessagePointer());
    }

    public DefaultValidateMessageProvider(List<ErrorMessagePointer> errorMessagePointerList) {
        if (!errorMessagePointerList.isEmpty()) {
            this.errorMessagePointerList.addAll(errorMessagePointerList);
        }
    }

    public List<ErrorMessagePointer> getErrorMessagePointerList() {
        return errorMessagePointerList;
    }

    @Override
    public ValidationMessage rewrite(WalkEvent walkEvent, ValidationMessage message) {
        JsonNode schemaNode = walkEvent.getSchemaNode();
        JsonNode error = schemaNode.at("/" + ERROR_KEY + "/" + message.getType());
        //如果在属性下面使用了显示声明的error,那么优先使用显示的error，然后使用全局的error.
        if (error.isObject() && error.size() == 0
                || error.getNodeType().equals(JsonNodeType.MISSING)) {
            String[] defaultErrorPoint = createErrorPropertyJsonPoint(message);
            if (defaultErrorPoint == null) {
                LOGGER.warn("找不到对应的ErrorMessagePointer,type:{},schema path:{},message:{}",
                        message.getType(), message.getSchemaPath(), message.getMessage());
                return message;
            }
            //如果没有自定义的error信息
            for (String point : defaultErrorPoint) {
                error = walkEvent.getParentSchema().findAncestor().getSchemaNode().at(point);
                if (!error.isEmpty()) {
                    break;
                }
            }
            //如果还是没找到那么返回最原始的message
            if (error.isEmpty()) {
                return message;
            }
        }
        //查找rootSchemaJsonNode
        JsonNode rootSchemaNode = findRootSchemaJsonNode(walkEvent);
        //处理error中的表达式或code与message
        error = findResolveMessage(rootSchemaNode, error);
        return buildValidationMessage(message, error);
    }


    protected JsonNode findRootSchemaJsonNode(WalkEvent walkEvent) {
        JsonSchema ancestor = walkEvent.getParentSchema().findAncestor();
        return ancestor.getSchemaNode();
    }

    private ValidationMessage buildValidationMessage(ValidationMessage message, JsonNode error) {
        Optional<String> codeOptional = Optional.ofNullable(error.get(ERROR_CODE_KEY)).map(JsonNode::asText);
        Optional<String> messageTextOptional = Optional.ofNullable(error.get(ERROR_MESSAGE_KEY)).map(JsonNode::asText);
        String code = codeOptional.orElse(message.getCode());
        String messageText = messageTextOptional.orElse(message.getMessage());
        return rewriteValidationMessage(message, code, messageText);
    }

    /**
     * 使用新的CODE码和错误信息替换掉原始信息
     *
     * @param message
     * @param code
     * @param errorText
     * @return
     */
    private ValidationMessage rewriteValidationMessage(ValidationMessage message, String code, String errorText) {
        return new ValidationMessage.Builder()
                .type(message.getType())
                .path(message.getPath())
                .code(code)
                .arguments(message.getArguments())
                .details(message.getDetails())
                .schemaPath(message.getSchemaPath())
                .customMessage(errorText)
                .format(new MessageFormat("")).build();
    }

    private String[] createErrorPropertyJsonPoint(ValidationMessage message) {
        String errorPropertyPoint = null;
        for (ErrorMessagePointer processor : errorMessagePointerList) {
            PointFactor pointFactor = new PointFactor(message);
            boolean support = processor.isSupport(pointFactor);
            if (support) {
                errorPropertyPoint = processor.createPointer(message);
                break;
            }
        }
        if (errorPropertyPoint == null || StringUtils.isBlank(errorPropertyPoint)) {
            return null;
        }
        return errorPropertyPoint.split(",");
    }

    private boolean isExpression(String value) {
        return value != null && value.startsWith("${") && value.endsWith("}");
    }

    private String resolveExpressionJsonPoint(String value) {
        String point = value.replace("${", "").replace("}", "");
        return point.startsWith("/" + ERROR_KEY) ? point : "/" + ERROR_KEY + point;
    }

    /**
     * 递规查找错误信息和其中的引用,支持以下几种格式
     * <p>
     * example:
     * {"error":"${/error/name}"};
     * {"error":{"type":{"code":"1234","message":"名字不能为空"}}};
     * {"error":{"type":"${/error/name/type}"}};
     * {"error":{"type":{"code":"${/error/name/type/code}","message":"名字不能为空"}}};
     * {"error":{"type":{"code":"${/error/name/type/code}","message":"${/error/name/type/message}"}}};
     *
     * @param root
     * @param error
     * @return
     */
    private JsonNode findResolveMessage(JsonNode root, JsonNode error) {
        if (error.isObject()) {
            JsonNode code = error.get(ERROR_CODE_KEY);
            JsonNode message = error.get(ERROR_MESSAGE_KEY);
            if (!isExpression(code.asText()) && !isExpression(message.asText())) {
                return error;
            }
            ObjectNode result = new ObjectNode(new JsonNodeFactory(true));
            if (isExpression(code.asText())) {
                code = findResolveMessage(root, code);
            }
            if (isExpression(message.asText())) {
                message = findResolveMessage(root, message);
            }
            result.set(ERROR_CODE_KEY, code);
            result.set(ERROR_MESSAGE_KEY, message);
            return result;
        } else {
            if (isExpression(error.asText())) {
                String jsonPoint = resolveExpressionJsonPoint(error.asText());
                JsonNode at = root.at(jsonPoint);
                return findResolveMessage(root, at);
            } else {
                return error;
            }
        }
    }
}
