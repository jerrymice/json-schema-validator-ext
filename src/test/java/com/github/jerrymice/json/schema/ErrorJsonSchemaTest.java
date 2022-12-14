package com.github.jerrymice.json.schema;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.github.jerrymice.json.schema.listener.ErrorMessageRewriteWalkListener;
import com.github.jerrymice.json.schema.model.Customer;
import com.github.jerrymice.json.schema.model.CustomerExt;
import com.github.jerrymice.json.schema.model.Mate;
import com.google.inject.internal.util.Sets;
import com.networknt.schema.SchemaValidatorsConfig;
import com.networknt.schema.ValidationMessage;
import com.networknt.schema.ValidationResult;
import com.networknt.schema.ValidatorTypeCode;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public class ErrorJsonSchemaTest {
    protected ValidatorManager validatorManager = new ValidatorManager();
    private Set<String> includeKeyWordSet = new HashSet<>();

    @Before
    public void beforeValidatorManager() throws Exception {
        Set<String> excludeKeywords = Sets.newHashSet();
        excludeKeywords.add(ValidatorTypeCode.PROPERTIES.getValue());
        excludeKeywords.add(ValidatorTypeCode.NOT.getValue());
        excludeKeywords.add(ValidatorTypeCode.NOT_ALLOWED.getValue());
        excludeKeywords.add(ValidatorTypeCode.ONE_OF.getValue());
        excludeKeywords.add(ValidatorTypeCode.ALL_OF.getValue());
        excludeKeywords.add(ValidatorTypeCode.ANY_OF.getValue());
        Set<String> collect = Arrays.stream(ValidatorTypeCode.values()).map(ValidatorTypeCode::getValue).collect(Collectors.toSet());
        collect.removeAll(excludeKeywords);
        includeKeyWordSet.addAll(collect);
        initValidatorManager();
    }

    protected void initValidatorManager() throws Exception {
        SchemaValidatorsConfig defaultSchemaValidatorsConfig = validatorManager.createDefaultSchemaValidatorsConfig();
        ErrorMessageRewriteWalkListener errorMessageRewriteWalkListener = new ErrorMessageRewriteWalkListener();
        includeKeyWordSet.forEach(i -> {
            defaultSchemaValidatorsConfig.addKeywordWalkListener(i, errorMessageRewriteWalkListener);
        });
        validatorManager.setSchemaValidatorsConfig(defaultSchemaValidatorsConfig);
        validatorManager.setSchemaFilePath("/ErrorMessageSchema.json");
        validatorManager.initJsonSchema();
    }

    private void assertNoSourceValidateMessage(Set<ValidationMessage> validationMessages) {
        long count = validationMessages.stream().filter(i -> i.getMessage().startsWith("$")).count();
        Assert.assertEquals(count, 0);
    }

    /**
     * ???????????????name
     *
     * @throws Exception
     */
    @Test
    public void nameSexRequired() throws Exception {
        validatorManager.getObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL);
        CustomerExt build = CustomerExt.builder().name(null).sex(null).work(1).city("??????").age(20).build();
        ValidationResult result = validatorManager.walk(build, "??????name?????????", true);
        Assert.assertEquals(result.getValidationMessages().size(), 2);
        validatorManager.getObjectMapper().setSerializationInclusion(JsonInclude.Include.ALWAYS);
        assertNoSourceValidateMessage(result.getValidationMessages());
    }


    /**
     * ??????minLength
     *
     * @throws Exception
     */
    @Test
    public void nameMinLength() throws Exception {
        CustomerExt build = CustomerExt.builder().name("???").sex(false).age(20).work(1).city("??????").build();
        ValidationResult result = validatorManager.walk(build, "??????name?????????", true);
        Assert.assertEquals(result.getValidationMessages().size(), 1);
        assertNoSourceValidateMessage(result.getValidationMessages());
    }

    /**
     * ??????minLength
     *
     * @throws Exception
     */
    @Test
    public void nameMaxLength() throws Exception {
        CustomerExt build = CustomerExt.builder().name("??????????????????????????????????????????").sex(false).age(20).work(1).city("??????").build();
        ValidationResult result = validatorManager.walk(build, "??????name?????????", true);
        Assert.assertEquals(result.getValidationMessages().size(), 1);
        assertNoSourceValidateMessage(result.getValidationMessages());
    }

    /**
     * ??????minLength
     *
     * @throws Exception
     */
    @Test
    public void nameType() throws Exception {
        HashMap<String, Object> map = new HashMap<>();
        map.put("name", 1233);
        map.put("sex", true);
        map.put("age", 30);
        map.put("work", 1);
        map.put("city", "??????");
        ValidationResult result = validatorManager.walk(map, "??????name?????????", true);
        Assert.assertEquals(result.getValidationMessages().size(), 1);
        assertNoSourceValidateMessage(result.getValidationMessages());
    }

    /**
     * ??????minLength
     *
     * @throws Exception
     */
    @Test
    public void sexType() throws Exception {
        HashMap<String, Object> map = new HashMap<>();
        map.put("name", "?????????");
        map.put("sex", "1");
        map.put("age", 30);
        map.put("work", 1);
        map.put("city", "??????");
        ValidationResult result = validatorManager.walk(map, "??????name?????????", true);
        Assert.assertEquals(result.getValidationMessages().size(), 1);
        assertNoSourceValidateMessage(result.getValidationMessages());
    }

    /**
     * ??????minLength
     *
     * @throws Exception
     */
    @Test
    public void sexNull() throws Exception {
        HashMap<String, Object> map = new HashMap<>();
        map.put("name", "?????????");
        map.put("sex", null);
        map.put("age", 30);
        map.put("work", 1);
        map.put("city", "??????");
        ValidationResult result = validatorManager.walk(map, "??????name?????????", true);
        Assert.assertEquals(result.getValidationMessages().size(), 1);
        assertNoSourceValidateMessage(result.getValidationMessages());
    }


    /**
     * ??????age??????
     *
     * @throws Exception
     */
    @Test
    public void ageMinimum() throws Exception {
        CustomerExt build = CustomerExt.builder().name("?????????").sex(true).age(-1).work(1).city("??????").build();
        ValidationResult result = validatorManager.walk(build, "??????age??????", true);
        Assert.assertEquals(result.getValidationMessages().size(), 1);
        assertNoSourceValidateMessage(result.getValidationMessages());
    }

    /**
     * ??????age??????
     *
     * @throws Exception
     */
    @Test
    public void ageMaximum() throws Exception {
        CustomerExt build = CustomerExt.builder().name("?????????").sex(true).age(120).work(1).city("??????").build();
        ValidationResult result = validatorManager.walk(build, "??????age??????", true);
        Assert.assertEquals(result.getValidationMessages().size(), 1);
        assertNoSourceValidateMessage(result.getValidationMessages());
    }

    /**
     * ??????age??????
     *
     * @throws Exception
     */
    @Test
    public void workEnum() throws Exception {
        CustomerExt build = CustomerExt.builder().name("?????????").work(3).city("??????").sex(true).age(110).build();
        ValidationResult result = validatorManager.walk(build, "??????age??????", true);
        Assert.assertEquals(result.getValidationMessages().size(), 1);
        assertNoSourceValidateMessage(result.getValidationMessages());
    }

    /**
     * ??????age??????
     *
     * @throws Exception
     */
    @Test
    public void workNull() throws Exception {
        CustomerExt build = CustomerExt.builder().name("?????????").work(null).city("??????").sex(true).age(110).build();
        ValidationResult result = validatorManager.walk(build, "??????age??????", true);
        Assert.assertEquals(result.getValidationMessages().size(), 0);
        assertNoSourceValidateMessage(result.getValidationMessages());
    }

    /**
     * ??????age??????
     *
     * @throws Exception
     */
    @Test
    public void cityConst() throws Exception {
        CustomerExt build = CustomerExt.builder().name("?????????").work(1).city("?????????").sex(true).age(110).build();
        ValidationResult result = validatorManager.walk(build, "??????age??????", true);
        Assert.assertEquals(result.getValidationMessages().size(), 1);
        assertNoSourceValidateMessage(result.getValidationMessages());
    }

    /**
     * ??????age??????
     *
     * @throws Exception
     */
    @Test
    public void cityNull() throws Exception {
        CustomerExt build = CustomerExt.builder().name("?????????").work(1).city(null).sex(true).age(110).build();
        ValidationResult result = validatorManager.walk(build, "??????age??????", true);
        Assert.assertEquals(result.getValidationMessages().size(), 1);
        assertNoSourceValidateMessage(result.getValidationMessages());
    }

    @Test
    public void marriageEnum() throws Exception {
        CustomerExt build = CustomerExt.builder().name("?????????").work(1).city("??????").sex(true).marriage(3).age(110).build();
        ValidationResult result = validatorManager.walk(build, "??????age??????", true);
        Assert.assertEquals(result.getValidationMessages().size(), 1);
        assertNoSourceValidateMessage(result.getValidationMessages());
    }

    @Test
    public void mateAgeMinimum() throws Exception {
        CustomerExt build = CustomerExt.builder().name("?????????").work(1).city("??????").sex(true).marriage(1)
                .mate(Mate.builder().age(19).name("??????").sex(true).build()).age(110).build();
        ValidationResult result = validatorManager.walk(build, "??????age??????", true);
        Assert.assertEquals(result.getValidationMessages().size(), 1);
        assertNoSourceValidateMessage(result.getValidationMessages());
    }

    @Test
    public void mateAgeMaximum() throws Exception {
        CustomerExt build = CustomerExt.builder().name("?????????").work(1).city("??????").sex(true).marriage(1)
                .mate(Mate.builder().age(120).name("??????").sex(true).build()).age(110).build();
        ValidationResult result = validatorManager.walk(build, "??????age??????", true);
        Assert.assertEquals(result.getValidationMessages().size(), 1);
        assertNoSourceValidateMessage(result.getValidationMessages());
    }

    @Test
    public void mateNameMinLength() throws Exception {
        CustomerExt build = CustomerExt.builder().name("?????????").work(1).city("??????").sex(true).marriage(1)
                .mate(Mate.builder().age(22).name("???").sex(true).build()).age(110).build();
        ValidationResult result = validatorManager.walk(build, "??????age??????", true);
        Assert.assertEquals(result.getValidationMessages().size(), 1);
        assertNoSourceValidateMessage(result.getValidationMessages());
    }

    @Test
    public void mateNameMaxLength() throws Exception {
        CustomerExt build = CustomerExt.builder().name("?????????").work(1).city("??????").sex(true).marriage(1)
                .mate(Mate.builder().age(22).name("?????????????????????????????????????????????????????????").sex(true).build()).age(110).build();
        ValidationResult result = validatorManager.walk(build, "??????age??????", true);
        Assert.assertEquals(result.getValidationMessages().size(), 1);
        assertNoSourceValidateMessage(result.getValidationMessages());
    }

    /**
     * ??????minLength
     *
     * @throws Exception
     */
    @Test
    public void metaSexType() throws Exception {
        HashMap<String, Object> map = new HashMap<>();
        map.put("name", "?????????");
        map.put("sex", true);
        map.put("age", 22);
        map.put("work", 1);
        map.put("city", "??????");
        HashMap<String, Object> mate = new HashMap<>();
        mate.put("name", "??????");
        mate.put("sex", 123);
        mate.put("age", 20);
        map.put("mate", mate);
        ValidationResult result = validatorManager.walk(map, "??????name?????????", true);
        Assert.assertEquals(result.getValidationMessages().size(), 1);
        assertNoSourceValidateMessage(result.getValidationMessages());
    }

    /**
     * ??????minLength
     *
     * @throws Exception
     */
    @Test
    public void metaSexNull() throws Exception {
        HashMap<String, Object> map = new HashMap<>();
        map.put("name", "?????????");
        map.put("sex", true);
        map.put("age", 22);
        map.put("work", 1);
        map.put("city", "??????");
        HashMap<String, Object> mate = new HashMap<>();
        mate.put("name", "??????");
        mate.put("sex", null);
        mate.put("age", 20);
        map.put("mate", mate);
        ValidationResult result = validatorManager.walk(map, "??????name?????????", true);
        Assert.assertEquals(result.getValidationMessages().size(), 1);
        assertNoSourceValidateMessage(result.getValidationMessages());
    }


}
