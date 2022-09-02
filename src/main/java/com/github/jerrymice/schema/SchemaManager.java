package com.github.jerrymice.schema;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.JsonMetaSchema;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.NonValidationKeyword;
import com.networknt.schema.SchemaValidatorsConfig;
import com.networknt.schema.SpecVersion;

public class SchemaManager {
    private static final String V202012_EXT_V1 = "https://json-schema.org/draft/2020-12-ext-v1/schema";

    /**
     * @param jsonNode
     * @param config
     * @return
     */
    public static JsonSchema getSchema(JsonNode jsonNode, SchemaValidatorsConfig config) {
        JsonSchemaFactory jsonSchemaFactory = createExtJsonSchemaFactory();
        return jsonSchemaFactory.getSchema(jsonNode, config);
    }

    /**
     * @return
     */
    private static JsonSchemaFactory createExtJsonSchemaFactory() {
        JsonMetaSchema v202012 = JsonMetaSchema.getV202012();
        JsonMetaSchema customSchema = createV2012ExtJsonMetaSchema(v202012);
        JsonSchemaFactory parent = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012);
        return JsonSchemaFactory.builder(parent)
                .addMetaSchema(customSchema).build();
    }

    /**
     * @param v202012
     * @return
     */
    private static JsonMetaSchema createV2012ExtJsonMetaSchema(JsonMetaSchema v202012) {
        //自定义一个版本，并添加else关键字
        return JsonMetaSchema.builder(V202012_EXT_V1, v202012)
                .addKeyword(new NonValidationKeyword("else"))
                .build();
    }
}
