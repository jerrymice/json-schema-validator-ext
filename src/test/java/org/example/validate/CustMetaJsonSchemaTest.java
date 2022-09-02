package org.example.validate;

import com.networknt.schema.ValidationMessage;
import lombok.extern.slf4j.Slf4j;
import org.example.validate.model.Customer;
import org.example.validate.model.Mate;
import org.junit.Assert;
import org.junit.Test;

import java.util.Set;


@Slf4j
public class CustMetaJsonSchemaTest {
    private ValidatorManager validatorManager = new ValidatorManager();

    /**
     * 验证女同
     *
     * @throws Exception
     */
    @Test
    public void validateGirlToGirl() throws Exception {
        Customer customer = Customer.builder().name("白合A").age(20).sex(false).marriage(1)
                .mate(Mate.builder().sex(false).age(22).name("白合B").build()).build();
        Set<ValidationMessage> result = validatorManager.validateCustomer(customer, "验证女同");
        Assert.assertTrue(result.size() == 1);
    }
}
