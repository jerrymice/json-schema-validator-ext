package com.github.jerrymice.json.schema;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.github.jerrymice.json.schema.listener.ErrorMessageRewriteWalkListener;
import com.github.jerrymice.json.schema.model.Customer;
import com.networknt.schema.SchemaValidatorsConfig;
import com.networknt.schema.ValidationMessage;
import com.networknt.schema.ValidationResult;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Set;

@Slf4j
public class ErrorJsonSchemaTest {
    protected ValidatorManager validatorManager = new ValidatorManager();


    @Before
    public void beforeValidatorManager() throws Exception {
        initValidatorManager();
    }

    protected void initValidatorManager() throws Exception {
        SchemaValidatorsConfig defaultSchemaValidatorsConfig = validatorManager.createDefaultSchemaValidatorsConfig();
        ErrorMessageRewriteWalkListener errorMessageRewriteWalkListener = new ErrorMessageRewriteWalkListener();
        defaultSchemaValidatorsConfig.addPropertyWalkListener(errorMessageRewriteWalkListener);
        validatorManager.setSchemaValidatorsConfig(defaultSchemaValidatorsConfig);
        validatorManager.setSchemaFilePath("/ErrorMessageSchema.json");
        validatorManager.initJsonSchema();


    }


    /**
     * 验证不存在name
     *
     * @throws Exception
     */
    @Test
    public void nameRequired() throws Exception {
        validatorManager.getObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL);
        Customer build = Customer.builder().name(null).sex(null).age(20).build();
        ValidationResult result = validatorManager.walk(build, "验证name不存在", true);
        Assert.assertEquals(result.getValidationMessages().size(), 1);
        validatorManager.getObjectMapper().setSerializationInclusion(JsonInclude.Include.ALWAYS);
    }

    /**
     * 验证age为空
     *
     * @throws Exception
     */
    @Test
    public void ageNull() throws Exception {
        Customer build = Customer.builder().name("涂铭鉴").sex(true).age(null).build();
        Set<ValidationMessage> validate = validatorManager.validate(build, "验证age为空");
        Assert.assertEquals(validate.size(), 1);
    }


    @Test
    public void walkAgeNull() throws Exception {
        Customer build = Customer.builder().name("涂铭鉴").sex(true).age(null).build();
        ValidationResult result = validatorManager.walk(build, "验证age为空", true);
        Assert.assertEquals(result.getValidationMessages().size(), 1);
    }

}
