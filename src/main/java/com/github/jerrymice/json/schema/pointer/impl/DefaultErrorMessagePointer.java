package com.github.jerrymice.json.schema.pointer.impl;

import com.github.jerrymice.json.schema.pointer.ErrorMessagePointer;
import com.github.jerrymice.json.schema.pointer.PointFactor;
import com.networknt.schema.ValidationMessage;
import com.networknt.schema.ValidatorTypeCode;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static com.github.jerrymice.json.schema.KeyWordExt.ERROR_KEY;

public class DefaultErrorMessagePointer implements ErrorMessagePointer {
    private Map<String, Function<ValidationMessage, String>> typeErrorPointMap = new HashMap<>();

    public DefaultErrorMessagePointer() {
        typeErrorPointMap.put(ValidatorTypeCode.REQUIRED.getValue(), this::typeRequired);
    }

    @Override
    public boolean isSupport(PointFactor pointFactor) {
        return true;
    }

    @Override
    public String createPointer(ValidationMessage validationMessage) {
        //type为integer,number,string,bool,object,array等，存在属性名，但属性值为null
        if (assertTypeNull(validationMessage)) {
            return typeNull(validationMessage);
        }
        //特殊情况处理
        Function<ValidationMessage, String> typeFunction = typeErrorPointMap.get(validationMessage.getType());
        if (typeFunction != null) {
            return typeFunction.apply(validationMessage);
        }
        //默认取值
        return defaultPoint(validationMessage);
    }

    private boolean assertTypeNull(ValidationMessage message) {
        return message.getType().equals(ValidatorTypeCode.TYPE.getValue()) &&
                message.getArguments() != null &&
                message.getArguments().length == 2 &&
                message.getArguments()[0].equals("null");
    }

    private String typeNull(ValidationMessage message) {
        return defaultPoint(message).replace("/" +
                ValidatorTypeCode.TYPE.getValue(), "/" +
                ValidatorTypeCode.REQUIRED.getValue()) + "," + defaultPoint(message);
    }

    private String typeRequired(ValidationMessage message) {
        String nodeAt = message.getSchemaPath()
                .replace("#/" + message.getType(), "")
                .replace("/" + message.getType(), "");
        nodeAt = nodeAt.equals("") ? "" : nodeAt + "/";
        return "/" + ERROR_KEY + "/" + nodeAt + message.getArguments()[0] + "/" +
                ValidatorTypeCode.REQUIRED.getValue();
    }

    private String defaultPoint(ValidationMessage message) {
        String schemaPath = message.getSchemaPath();
        return "/" + ERROR_KEY + schemaPath
                .replace("#/" + ValidatorTypeCode.PROPERTIES.getValue(), "")
                .replace("/" + ValidatorTypeCode.PROPERTIES.getValue() + "/", "/");
    }


}
