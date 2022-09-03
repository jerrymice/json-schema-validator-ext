package org.example.validate;

import com.networknt.schema.ValidationMessage;
import lombok.extern.slf4j.Slf4j;
import org.example.validate.model.Customer;
import org.junit.Test;

import java.util.Set;


@Slf4j
public class CustMetaJsonSchemaTest extends CustomerJsonSchemaTest {
    @Override
    protected void initValidatorManager() throws Exception {
        validatorManager.setSchemaFilePath("/CustMetaSchema.json");
        validatorManager.initJsonSchema();
    }
}
