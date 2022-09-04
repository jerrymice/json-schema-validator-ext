package com.github.jerrymice.json.schema;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.github.jerrymice.json.schema.listener.ErrorMessageRewriteWalkListener;
import com.github.jerrymice.json.schema.model.Customer;
import com.github.jerrymice.json.schema.model.Mate;
import com.google.inject.internal.util.Sets;
import com.networknt.schema.SchemaValidatorsConfig;
import com.networknt.schema.ValidationMessage;
import com.networknt.schema.ValidatorTypeCode;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;
import java.util.stream.Collectors;

public class ConditionErrorJsonSchemaTest {
    protected ValidatorManager validatorManager = new ValidatorManager();
    Set<String> excludeKeywords = Sets.newHashSet();

    @Before
    public void beforeValidatorManager() throws Exception {
        excludeKeywords.add(ValidatorTypeCode.PROPERTIES.getValue());
        excludeKeywords.add(ValidatorTypeCode.NOT.getValue());
        excludeKeywords.add(ValidatorTypeCode.NOT_ALLOWED.getValue());
        excludeKeywords.add(ValidatorTypeCode.ONE_OF.getValue());
        excludeKeywords.add(ValidatorTypeCode.ALL_OF.getValue());
        excludeKeywords.add(ValidatorTypeCode.ANY_OF.getValue());

        initValidatorManager();
    }

    protected void initValidatorManager() throws Exception {
        SchemaValidatorsConfig defaultSchemaValidatorsConfig = validatorManager.createDefaultSchemaValidatorsConfig();
        ErrorMessageRewriteWalkListener errorMessageRewriteWalkListener = new ErrorMessageRewriteWalkListener();
//        defaultSchemaValidatorsConfig.addPropertyWalkListener(errorMessageRewriteWalkListener);
        Set<String> collect = Arrays.stream(ValidatorTypeCode.values()).map(ValidatorTypeCode::getValue).collect(Collectors.toSet());
        collect.removeAll(excludeKeywords);
        for (String keyword : collect) {
            defaultSchemaValidatorsConfig.addKeywordWalkListener(keyword, errorMessageRewriteWalkListener);
        }
        validatorManager.setSchemaValidatorsConfig(defaultSchemaValidatorsConfig);
        validatorManager.setSchemaFilePath("/ConditionErrorMessageSchema.json");
        validatorManager.initJsonSchema();
    }

    private void assertNoSourceValidateMessage(Set<ValidationMessage> validationMessages) {
        long count = validationMessages.stream().filter(i -> i.getMessage().startsWith("$")).count();
        Assert.assertEquals(count, 0);
    }


    /**
     * 验证marriage为Null,mate不存在
     *
     * @throws Exception
     */
    @Test
    public void validateMarriageNull() throws Exception {
        HashMap<String, Object> customer = new HashMap<>();
        customer.put("name", "涂铭鉴");
        customer.put("age", 25);
        customer.put("sex", true);
        customer.put("marriage", null);
        Set<ValidationMessage> result = validatorManager.walk(customer, "验证marriage为Null,mate不存在", true)
                .getValidationMessages();
        Assert.assertEquals(result.size(), 0);
        assertNoSourceValidateMessage(result);
    }

    /**
     * 验证已婚,没有配偶信息
     *
     * @throws Exception
     */
    @Test
    public void validateMetaNoPropertyName() throws Exception {
        validatorManager.getObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL);
        Customer customer = Customer.builder().name("涂铭鉴").age(25).sex(true).marriage(1).build();
        Set<ValidationMessage> result = validatorManager.walk(customer, "验证已婚,没有配偶信息", true)
                .getValidationMessages();
        Assert.assertEquals(result.size(), 1);
        validatorManager.getObjectMapper().setSerializationInclusion(JsonInclude.Include.ALWAYS);
        assertNoSourceValidateMessage(result);
    }

    /**
     * 验证女配偶未满20岁
     *
     * @throws Exception
     */
    @Test
    public void validateMetaGirlAgeFail() throws Exception {
        Customer customer = Customer.builder().name("涂铭鉴").age(22).sex(true).marriage(1)
                .mate(Mate.builder().sex(false).age(19).name("杨幂").build()).build();
        Set<ValidationMessage> result = validatorManager.walk(customer, "验证女配偶未满20岁", true)
                .getValidationMessages();
        Assert.assertEquals(result.size(), 1);
        assertNoSourceValidateMessage(result);
    }

    /**
     * 验证女配偶已满20岁
     *
     * @throws Exception
     */
    @Test
    public void validateMetaGirlAgeSuccess() throws Exception {
        Customer customer = Customer.builder().name("涂铭鉴").age(22).sex(true).marriage(1)
                .mate(Mate.builder().sex(false).age(20).name("杨幂").build()).build();
        Set<ValidationMessage> result = validatorManager.walk(customer, "验证女配偶已满20岁", true)
                .getValidationMessages();
        Assert.assertEquals(result.size(), 0);
        assertNoSourceValidateMessage(result);
    }

    /**
     * 验证男配偶未满22岁
     *
     * @throws Exception
     */
    @Test
    public void validateMetaGirlBoyFail() throws Exception {
        Customer customer = Customer.builder().name("杨幂").age(20).sex(true).marriage(1)
                .mate(Mate.builder().sex(false).age(21).name("涂铭鉴").build()).build();
        Set<ValidationMessage> result = validatorManager.walk(customer, "验证男配偶未满22岁", true)
                .getValidationMessages();
        Assert.assertEquals(result.size(), 1);
    }

    /**
     * 验证男配偶已满22岁
     *
     * @throws Exception
     */
    @Test
    public void validateMetaGirlBoySuccess() throws Exception {
        Customer customer = Customer.builder().name("杨幂").age(20).sex(false).marriage(1)
                .mate(Mate.builder().sex(true).age(22).name("涂铭鉴").build()).build();
        Set<ValidationMessage> result = validatorManager.walk(customer, "验证男配偶已满22岁", true)
                .getValidationMessages();
        Assert.assertEquals(result.size(), 0);
        assertNoSourceValidateMessage(result);
    }

    /**
     * 验证男同
     *
     * @throws Exception
     */
    @Test
    public void validateBoyToBoy() throws Exception {
        Customer customer = Customer.builder().name("玻璃A").age(22).sex(true).marriage(1)
                .mate(Mate.builder().sex(true).age(22).name("玻璃B").build()).build();
        Set<ValidationMessage> result = validatorManager.walk(customer, "验证男同", true)
                .getValidationMessages();
        Assert.assertEquals(result.size(), 1);
        assertNoSourceValidateMessage(result);
    }

    /**
     * 验证女同
     *
     * @throws Exception
     */
    @Test
    public void validateGirlToGirl() throws Exception {
        Customer customer = Customer.builder().name("白合A").age(20).sex(false).marriage(1)
                .mate(Mate.builder().sex(false).age(22).name("白合B").build()).build();
        Set<ValidationMessage> result = validatorManager.walk(customer, "验证女同", true)
                .getValidationMessages();
        Assert.assertEquals(result.size(), 1);
        assertNoSourceValidateMessage(result);
    }

}
