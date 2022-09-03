package com.github.jerrymice.json.schema;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.github.jerrymice.json.schema.model.Customer;
import com.github.jerrymice.json.schema.model.Mate;
import com.networknt.schema.ValidationMessage;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Set;

@Slf4j
public class CustomerJsonSchemaTest {
    protected ValidatorManager validatorManager = new ValidatorManager();


    @Before
    public void beforeValidatorManager() throws Exception {
        initValidatorManager();
    }

    protected void initValidatorManager() throws Exception {
        validatorManager.setSchemaFilePath("/CustomerSchema.json");
        validatorManager.initJsonSchema();
    }


    protected Set<ValidationMessage> validate(Object javabean, String title) throws Exception {
        return validatorManager.validate(javabean, title);
    }

    /**
     * 验证没有age属性
     *
     * @throws Exception
     */
    @Test
    public void validateNoPropertyNameAgeRequired() throws Exception {
        validatorManager.getObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL);
        Customer customer = Customer.builder().name("涂铭鉴").sex(true).marriage(0).build();
        Set<ValidationMessage> result = validate(customer, "验证没有age属性");
        Assert.assertEquals(result.size(),1);
        validatorManager.getObjectMapper().setSerializationInclusion(JsonInclude.Include.ALWAYS);
    }

    /**
     * 验证有age属性，但age值为空
     *
     * @throws Exception
     */
    @Test
    public void validateAgeNullRequired() throws Exception {
        Customer customer = Customer.builder().name("涂铭鉴").sex(true).marriage(0).build();
        Set<ValidationMessage> result = validate(customer, "验证有age属性，但age值为空");
        Assert.assertEquals(result.size(),1);
    }

    /**
     * 验证有age,但小于1
     *
     * @throws Exception
     */
    @Test
    public void validateAgeMin() throws Exception {
        Customer customer = Customer.builder().name("涂铭鉴").age(-2).sex(true).marriage(0).build();
        Set<ValidationMessage> result = validate(customer, "验证有age,但小于1");
        Assert.assertEquals(result.size(),1);
    }

    /**
     * 验证有age超过120
     *
     * @throws Exception
     */
    @Test
    public void validateAgeMax() throws Exception {
        Customer customer = Customer.builder().name("涂铭鉴").age(120).sex(true).marriage(0).build();
        Set<ValidationMessage> result = validate(customer, "验证有age超过120");
        Assert.assertEquals(result.size(),1);
    }

    /**
     * 验证age正常
     *
     * @throws Exception
     */
    @Test
    public void validateAgeSuccess() throws Exception {
        Customer customer = Customer.builder().name("涂铭鉴").age(25).sex(true).marriage(0).build();
        Set<ValidationMessage> result = validate(customer, "验证age正常");
        Assert.assertEquals(result.size(),0);
    }


    /**
     * 验证男未满22岁结婚
     */
    @Test
    public void validateBoyMarriageAgeFail() throws Exception {
        Customer customer = Customer.builder().name("涂铭鉴").age(18).sex(true).marriage(1)
                .mate(Mate.builder().age(20).name("杨幂").sex(false).build()).build();
        Set<ValidationMessage> result = validate(customer, "验证男未满22岁结婚");
        Assert.assertEquals(result.size(),1);
    }

    /**
     * 验证男满22岁结婚
     */
    @Test
    public void validateBoyMarriageAgeSuccess() throws Exception {
        Customer customer = Customer.builder().name("涂铭鉴").age(22).sex(true).marriage(1)
                .mate(Mate.builder().age(20).name("杨幂").sex(false).build()).build();
        Set<ValidationMessage> result = validate(customer, "验证男满22岁结婚");
        Assert.assertEquals(result.size(),0);
    }

    /**
     * 验证女未满20岁结婚
     */
    @Test
    public void validateGirlMarriageAgeFail() throws Exception {
        Customer customer = Customer.builder().name("杨幂").age(19).sex(false).marriage(1)
                .mate(Mate.builder().age(22).name("涂铭鉴").sex(true).build()).build();
        Set<ValidationMessage> result = validate(customer, "验证女未满20岁结婚");
        Assert.assertEquals(result.size(),1);
    }

    /**
     * 验证女满20岁结婚
     */
    @Test
    public void validateGirlMarriageAgeSuccess() throws Exception {
        Customer customer = Customer.builder().name("杨幂").age(20).sex(false).marriage(1)
                .mate(Mate.builder().age(22).name("涂铭鉴").sex(true).build()).build();
        Set<ValidationMessage> result = validate(customer, "验证女满20岁结婚");
        Assert.assertEquals(result.size(),0);
    }

    /**
     * 验证没有marriage属性
     * @throws Exception
     */
    @Test
    public void validateMarriageNoPropertyName()throws Exception{
        validatorManager.getObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL);
        Customer customer = Customer.builder().name("涂铭鉴").age(25).sex(true).build();
        Set<ValidationMessage> result = validate(customer, "验证没有marriage属性");
        Assert.assertEquals(result.size(),0);
        validatorManager.getObjectMapper().setSerializationInclusion(JsonInclude.Include.ALWAYS);
    }

    /**
     * 验证marriage为Null,mate为null
     * @throws Exception
     */
    @Test
    public void validateMarriageAndMetaNull()throws Exception{
        Customer customer = Customer.builder().name("涂铭鉴").age(25).marriage(null).mate(null).sex(true).build();
        Set<ValidationMessage> result = validate(customer, "验证marriage为Null,mate为null");
        Assert.assertEquals(result.size(),0);
    }

    /**
     * 验证marriage为Null,mate不存在
     * @throws Exception
     */
    @Test
    public void validateMarriageNull()throws Exception{
        HashMap<String, Object> customer = new HashMap<>();
        customer.put("name","涂铭鉴");
        customer.put("age",25);
        customer.put("sex",true);
        customer.put("marriage",null);
        Set<ValidationMessage> result = validate(customer, "验证marriage为Null,mate不存在");
        Assert.assertEquals(result.size(),0);
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
        Set<ValidationMessage> result = validate(customer, "验证已婚,没有配偶信息");
        Assert.assertEquals(result.size(),1);
        validatorManager.getObjectMapper().setSerializationInclusion(JsonInclude.Include.ALWAYS);
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
        Set<ValidationMessage> result = validate(customer, "验证女配偶未满20岁");
        Assert.assertEquals(result.size(),1);
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
        Set<ValidationMessage> result = validate(customer, "验证女配偶已满20岁");
        Assert.assertEquals(result.size(),0);
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
        Set<ValidationMessage> result = validate(customer, "验证男配偶未满22岁");
        Assert.assertEquals(result.size(),1);
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
        Set<ValidationMessage> result = validate(customer, "验证男配偶已满22岁");
        Assert.assertEquals(result.size(),0);
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
        Set<ValidationMessage> result = validate(customer, "验证男同");
        Assert.assertEquals(result.size(),1);
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
        Set<ValidationMessage> result = validate(customer, "验证女同");
        Assert.assertEquals(result.size(),1);
    }

}
