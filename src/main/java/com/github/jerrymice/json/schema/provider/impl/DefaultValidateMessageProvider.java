package com.github.jerrymice.json.schema.provider.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.MissingNode;
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

import static com.github.jerrymice.json.schema.KeyWordExt.ERROR_CODE_KEY;
import static com.github.jerrymice.json.schema.KeyWordExt.ERROR_KEY;
import static com.github.jerrymice.json.schema.KeyWordExt.ERROR_MESSAGE_KEY;

public class DefaultValidateMessageProvider implements ValidateMessageProvider {
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
        //查找rootSchemaJsonNode
        JsonNode rootSchemaNode = findRootSchemaJsonNode(walkEvent);
        //查找已经在properties对应的属性名下明确定义的error
        JsonNode errorTypeNode = findExplicitDefErrorTypeNode(message.getType(), schemaNode, rootSchemaNode);
        //如果找不到errorTypeNode才使用通用的错误信息
        if (errorTypeNode.getNodeType().equals(JsonNodeType.MISSING)) {
            String[] defaultErrorPoint = createErrorPropertyJsonPoint(message);
            if (defaultErrorPoint == null) {
                LOGGER.warn("找不到对应的ErrorMessagePointer,type:{},schema path:{},message:{}",
                        message.getType(), message.getSchemaPath(), message.getMessage());
                return message;
            }
            //如果没有自定义的error信息
            for (String point : defaultErrorPoint) {
                errorTypeNode = rootSchemaNode.at(point);
                if (!errorTypeNode.isEmpty()) {
                    break;
                }
            }
            //如果还是没找到那么返回最原始的message
            if (errorTypeNode.isEmpty()) {
                return message;
            }
        }
        //error中的message与code属性还有${..}引用表达式，那么需要解析并处理
        errorTypeNode = findResolveMessage(rootSchemaNode, errorTypeNode);
        return buildValidationMessage(message, errorTypeNode);
    }

    /**
     * 查找已经在该节点下面明确定义的error属性
     * <p>
     * example:
     * {"error":"${/$error/name}"};
     * {"error":{"type":"${/$error/name/type}"}};
     *
     * @param type
     * @param schemaNode
     * @param rootSchemaNode
     * @return
     */
    protected JsonNode findExplicitDefErrorTypeNode(String type, JsonNode schemaNode,
                                                    JsonNode rootSchemaNode) {
        //通过error和type找明确定义的节点,{"error":{"type":"${/error/name/type}"}};
        JsonNode errorTypeNode = schemaNode.at("/" + ERROR_KEY + "/" + type);
        if (errorTypeNode.getNodeType().equals(JsonNodeType.MISSING)) {
            ////通过error找定义的节点，并查找表达式引用,{"error":"${/error/name}"};
            JsonNode errorNode = schemaNode.at("/" + ERROR_KEY);
            if (errorNode.getNodeType().equals(JsonNodeType.STRING)) {
                //再查找type对应的error:{"message","","code":""}
                errorTypeNode = findErrorNodeRef(rootSchemaNode, errorNode.asText(), type);
            } else {
                return MissingNode.getInstance();
            }
        }
        return errorTypeNode;
    }


    protected JsonNode findRootSchemaJsonNode(WalkEvent walkEvent) {
        if (walkEvent.getParentSchema() == null) {
            return walkEvent.getSchemaNode();
        }
        JsonSchema ancestor = walkEvent.getParentSchema().findAncestor();
        return ancestor.getSchemaNode();
    }

    /**
     * 递规查找错误信息和其中的引用,支持以下几种格式
     * <p>
     * example:
     * {"name":{"type":{"code":"1234","message":"名字不能为空"}}};
     * {"name":{"type":{"code":"${/$error/name/type/code}","message":"名字不能为空"}}};
     * {"name":{"type":{"code":"${/$error/name/type/code}","message":"${/$error/name/type/message}"}}};
     *
     * @param root
     * @param error
     * @return
     */
    protected JsonNode findResolveMessage(JsonNode root, JsonNode error) {
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

    private JsonNode findErrorNodeRef(JsonNode root, String rawExpression, String type) {
        String expressionJsonPoint = resolveExpressionJsonPoint(rawExpression);
        JsonNode node = root.at(expressionJsonPoint);
        if (node.getNodeType().equals(JsonNodeType.MISSING)) {
            return node;
        }
        if (node.getNodeType().equals(JsonNodeType.STRING)) {
            return findErrorNodeRef(root, node.toString(), type);
        }
        return node.at("/" + type);
    }
}
