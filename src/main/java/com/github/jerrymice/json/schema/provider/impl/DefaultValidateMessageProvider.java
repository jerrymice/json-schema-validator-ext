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
        //??????rootSchemaJsonNode
        JsonNode rootSchemaNode = findRootSchemaJsonNode(walkEvent);
        //???????????????properties????????????????????????????????????error
        JsonNode errorTypeNode = findExplicitDefErrorTypeNode(message.getType(), schemaNode, rootSchemaNode);
        //???????????????errorTypeNode??????????????????????????????
        if (errorTypeNode.getNodeType().equals(JsonNodeType.MISSING)) {
            String[] defaultErrorPoint = createErrorPropertyJsonPoint(message);
            if (defaultErrorPoint == null) {
                LOGGER.warn("??????????????????ErrorMessagePointer,type:{},schema path:{},message:{}",
                        message.getType(), message.getSchemaPath(), message.getMessage());
                return message;
            }
            //????????????????????????error??????
            for (String point : defaultErrorPoint) {
                errorTypeNode = rootSchemaNode.at(point);
                if (!errorTypeNode.isEmpty()) {
                    break;
                }
            }
            //?????????????????????????????????????????????message
            if (errorTypeNode.isEmpty()) {
                return message;
            }
        }
        //error??????message???code????????????${..}?????????????????????????????????????????????
        errorTypeNode = findResolveMessage(rootSchemaNode, errorTypeNode);
        return buildValidationMessage(message, errorTypeNode);
    }

    /**
     * ?????????????????????????????????????????????error??????
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
        //??????error???type????????????????????????,{"error":{"type":"${/error/name/type}"}};
        JsonNode errorTypeNode = schemaNode.at("/" + ERROR_KEY + "/" + type);
        if (errorTypeNode.getNodeType().equals(JsonNodeType.MISSING)) {
            ////??????error?????????????????????????????????????????????,{"error":"${/error/name}"};
            JsonNode errorNode = schemaNode.at("/" + ERROR_KEY);
            if (errorNode.getNodeType().equals(JsonNodeType.STRING)) {
                //?????????type?????????error:{"message","","code":""}
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
     * ??????????????????????????????????????????,????????????????????????
     * <p>
     * example:
     * {"name":{"type":{"code":"1234","message":"??????????????????"}}};
     * {"name":{"type":{"code":"${/$error/name/type/code}","message":"??????????????????"}}};
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
     * ????????????CODE???????????????????????????????????????
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
