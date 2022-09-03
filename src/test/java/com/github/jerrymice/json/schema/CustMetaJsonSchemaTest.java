package com.github.jerrymice.json.schema;

import lombok.extern.slf4j.Slf4j;


@Slf4j
public class CustMetaJsonSchemaTest extends CustomerJsonSchemaTest {
    @Override
    protected void initValidatorManager() throws Exception {
        validatorManager.setSchemaFilePath("/CustMetaSchema.json");
        validatorManager.initJsonSchema();
    }
}
