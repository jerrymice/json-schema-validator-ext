package com.github.jerrymice.json.schema;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.github.jerrymice.json.schema.listener.ErrorMessageRewriteWalkListener;
import com.github.jerrymice.json.schema.model.Customer;
import com.github.jerrymice.json.schema.model.CustomerExt;
import com.github.jerrymice.json.schema.model.Mate;
import com.networknt.schema.SchemaValidatorsConfig;
import com.networknt.schema.ValidationMessage;
import com.networknt.schema.ValidationResult;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
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

    private void assertNoSourceValidateMessage(Set<ValidationMessage> validationMessages) {
        long count = validationMessages.stream().filter(i -> i.getMessage().startsWith("$")).count();
        Assert.assertEquals(count, 0);
    }

    /**
     * 验证不存在name
     *
     * @throws Exception
     */
    @Test
    public void nameSexRequired() throws Exception {
        validatorManager.getObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL);
        CustomerExt build = CustomerExt.builder().name(null).sex(null).work(1).city("成都").age(20).build();
        ValidationResult result = validatorManager.walk(build, "验证name不存在", true);
        Assert.assertEquals(result.getValidationMessages().size(), 2);
        validatorManager.getObjectMapper().setSerializationInclusion(JsonInclude.Include.ALWAYS);
        assertNoSourceValidateMessage(result.getValidationMessages());
    }


    /**
     * 验证minLength
     *
     * @throws Exception
     */
    @Test
    public void nameMinLength() throws Exception {
        CustomerExt build = CustomerExt.builder().name("涂").sex(false).age(20).work(1).city("成都").build();
        ValidationResult result = validatorManager.walk(build, "验证name不存在", true);
        Assert.assertEquals(result.getValidationMessages().size(), 1);
        assertNoSourceValidateMessage(result.getValidationMessages());
    }

    /**
     * 验证minLength
     *
     * @throws Exception
     */
    @Test
    public void nameMaxLength() throws Exception {
        CustomerExt build = CustomerExt.builder().name("涂涂涂涂涂涂涂涂涂涂涂涂涂涂").sex(false).age(20).work(1).city("成都").build();
        ValidationResult result = validatorManager.walk(build, "验证name不存在", true);
        Assert.assertEquals(result.getValidationMessages().size(), 1);
        assertNoSourceValidateMessage(result.getValidationMessages());
    }

    /**
     * 验证minLength
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
        map.put("city", "成都");
        ValidationResult result = validatorManager.walk(map, "验证name不存在", true);
        Assert.assertEquals(result.getValidationMessages().size(), 1);
        assertNoSourceValidateMessage(result.getValidationMessages());
    }

    /**
     * 验证minLength
     *
     * @throws Exception
     */
    @Test
    public void sexType() throws Exception {
        HashMap<String, Object> map = new HashMap<>();
        map.put("name", "涂铭鉴");
        map.put("sex", "1");
        map.put("age", 30);
        map.put("work", 1);
        map.put("city", "成都");
        ValidationResult result = validatorManager.walk(map, "验证name不存在", true);
        Assert.assertEquals(result.getValidationMessages().size(), 1);
        assertNoSourceValidateMessage(result.getValidationMessages());
    }

    /**
     * 验证minLength
     *
     * @throws Exception
     */
    @Test
    public void sexNull() throws Exception {
        HashMap<String, Object> map = new HashMap<>();
        map.put("name", "涂铭鉴");
        map.put("sex", null);
        map.put("age", 30);
        map.put("work", 1);
        map.put("city", "成都");
        ValidationResult result = validatorManager.walk(map, "验证name不存在", true);
        Assert.assertEquals(result.getValidationMessages().size(), 1);
        assertNoSourceValidateMessage(result.getValidationMessages());
    }


    /**
     * 验证age为空
     *
     * @throws Exception
     */
    @Test
    public void ageMinimum() throws Exception {
        CustomerExt build = CustomerExt.builder().name("涂铭鉴").sex(true).age(-1).work(1).city("成都").build();
        ValidationResult result = validatorManager.walk(build, "验证age为空", true);
        Assert.assertEquals(result.getValidationMessages().size(), 1);
        assertNoSourceValidateMessage(result.getValidationMessages());
    }

    /**
     * 验证age为空
     *
     * @throws Exception
     */
    @Test
    public void ageMaximum() throws Exception {
        CustomerExt build = CustomerExt.builder().name("涂铭鉴").sex(true).age(120).work(1).city("成都").build();
        ValidationResult result = validatorManager.walk(build, "验证age为空", true);
        Assert.assertEquals(result.getValidationMessages().size(), 1);
        assertNoSourceValidateMessage(result.getValidationMessages());
    }

    /**
     * 验证age为空
     *
     * @throws Exception
     */
    @Test
    public void workEnum() throws Exception {
        CustomerExt build = CustomerExt.builder().name("涂铭鉴").work(3).city("成都").sex(true).age(110).build();
        ValidationResult result = validatorManager.walk(build, "验证age为空", true);
        Assert.assertEquals(result.getValidationMessages().size(), 1);
        assertNoSourceValidateMessage(result.getValidationMessages());
    }

    /**
     * 验证age为空
     *
     * @throws Exception
     */
    @Test
    public void workNull() throws Exception {
        CustomerExt build = CustomerExt.builder().name("涂铭鉴").work(null).city("成都").sex(true).age(110).build();
        ValidationResult result = validatorManager.walk(build, "验证age为空", true);
        Assert.assertEquals(result.getValidationMessages().size(), 0);
        assertNoSourceValidateMessage(result.getValidationMessages());
    }

    /**
     * 验证age为空
     *
     * @throws Exception
     */
    @Test
    public void cityConst() throws Exception {
        CustomerExt build = CustomerExt.builder().name("涂铭鉴").work(1).city("成成成").sex(true).age(110).build();
        ValidationResult result = validatorManager.walk(build, "验证age为空", true);
        Assert.assertEquals(result.getValidationMessages().size(), 1);
        assertNoSourceValidateMessage(result.getValidationMessages());
    }

    /**
     * 验证age为空
     *
     * @throws Exception
     */
    @Test
    public void cityNull() throws Exception {
        CustomerExt build = CustomerExt.builder().name("涂铭鉴").work(1).city(null).sex(true).age(110).build();
        ValidationResult result = validatorManager.walk(build, "验证age为空", true);
        Assert.assertEquals(result.getValidationMessages().size(), 1);
        assertNoSourceValidateMessage(result.getValidationMessages());
    }

    @Test
    public void marriageEnum() throws Exception {
        CustomerExt build = CustomerExt.builder().name("涂铭鉴").work(1).city("成都").sex(true).marriage(3).age(110).build();
        ValidationResult result = validatorManager.walk(build, "验证age为空", true);
        Assert.assertEquals(result.getValidationMessages().size(), 1);
        assertNoSourceValidateMessage(result.getValidationMessages());
    }

    @Test
    public void mateAgeMinimum() throws Exception {
        CustomerExt build = CustomerExt.builder().name("涂铭鉴").work(1).city("成都").sex(true).marriage(1)
                .mate(Mate.builder().age(19).name("杨幂").sex(true).build()).age(110).build();
        ValidationResult result = validatorManager.walk(build, "验证age为空", true);
        Assert.assertEquals(result.getValidationMessages().size(), 1);
        assertNoSourceValidateMessage(result.getValidationMessages());
    }

    @Test
    public void mateAgeMaximum() throws Exception {
        CustomerExt build = CustomerExt.builder().name("涂铭鉴").work(1).city("成都").sex(true).marriage(1)
                .mate(Mate.builder().age(120).name("杨幂").sex(true).build()).age(110).build();
        ValidationResult result = validatorManager.walk(build, "验证age为空", true);
        Assert.assertEquals(result.getValidationMessages().size(), 1);
        assertNoSourceValidateMessage(result.getValidationMessages());
    }

    @Test
    public void mateNameMinLength() throws Exception {
        CustomerExt build = CustomerExt.builder().name("涂铭鉴").work(1).city("成都").sex(true).marriage(1)
                .mate(Mate.builder().age(22).name("幂").sex(true).build()).age(110).build();
        ValidationResult result = validatorManager.walk(build, "验证age为空", true);
        Assert.assertEquals(result.getValidationMessages().size(), 1);
        assertNoSourceValidateMessage(result.getValidationMessages());
    }

    @Test
    public void mateNameMaxLength() throws Exception {
        CustomerExt build = CustomerExt.builder().name("涂铭鉴").work(1).city("成都").sex(true).marriage(1)
                .mate(Mate.builder().age(22).name("杨幂幂幂幂幂幂幂幂幂幂幂幂幂幂幂幂幂幂").sex(true).build()).age(110).build();
        ValidationResult result = validatorManager.walk(build, "验证age为空", true);
        Assert.assertEquals(result.getValidationMessages().size(), 1);
        assertNoSourceValidateMessage(result.getValidationMessages());
    }

    /**
     * 验证minLength
     *
     * @throws Exception
     */
    @Test
    public void metaSexType() throws Exception {
        HashMap<String, Object> map = new HashMap<>();
        map.put("name", "涂铭鉴");
        map.put("sex", true);
        map.put("age", 22);
        map.put("work", 1);
        map.put("city", "成都");
        HashMap<String, Object> mate = new HashMap<>();
        mate.put("name", "杨幂");
        mate.put("sex", 123);
        mate.put("age", 20);
        map.put("mate", mate);
        ValidationResult result = validatorManager.walk(map, "验证name不存在", true);
        Assert.assertEquals(result.getValidationMessages().size(), 1);
        assertNoSourceValidateMessage(result.getValidationMessages());
    }

    /**
     * 验证minLength
     *
     * @throws Exception
     */
    @Test
    public void metaSexNull() throws Exception {
        HashMap<String, Object> map = new HashMap<>();
        map.put("name", "涂铭鉴");
        map.put("sex", true);
        map.put("age", 22);
        map.put("work", 1);
        map.put("city", "成都");
        HashMap<String, Object> mate = new HashMap<>();
        mate.put("name", "杨幂");
        mate.put("sex", null);
        mate.put("age", 20);
        map.put("mate", mate);
        ValidationResult result = validatorManager.walk(map, "验证name不存在", true);
        Assert.assertEquals(result.getValidationMessages().size(), 1);
        assertNoSourceValidateMessage(result.getValidationMessages());
    }


}
