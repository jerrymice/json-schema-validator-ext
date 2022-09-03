package com.github.jerrymice.json.schema;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.SchemaValidatorsConfig;
import com.networknt.schema.ValidationMessage;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

@Slf4j
public class ValidatorManager {

    private ObjectMapper objectMapper = new ObjectMapper();

    private String schemaFilePath;

    private JsonSchema jsonSchema;

    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String getSchemaFilePath() {
        return schemaFilePath;
    }

    public void setSchemaFilePath(String schemaFilePath) {
        this.schemaFilePath = schemaFilePath;
    }

    public JsonSchema getJsonSchema() {
        return jsonSchema;
    }

    public void setJsonSchema(JsonSchema jsonSchema) {
        this.jsonSchema = jsonSchema;
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    /**
     * @param javabean
     * @param title
     * @return
     * @throws Exception
     */
    public Set<ValidationMessage> validate(Object javabean, String title) throws Exception {
        initJsonSchema();
        return validate(javabean, title, jsonSchema);
    }

    public void initJsonSchema() throws Exception {
        if (jsonSchema == null) {
            jsonSchema = createJsonSchemaByFile();
        }
    }

    /**
     * @param javabean
     * @param title
     * @return
     * @throws Exception
     */
    private Set<ValidationMessage> validate(Object javabean, String title, JsonSchema jsonSchema) throws Exception {
        String json = objectMapper.writeValueAsString(javabean);
        JsonNode customerJsonNode = objectMapper.readTree(json);
        Set<ValidationMessage> validate = jsonSchema.validate(customerJsonNode);
        System.out.println();
        logging(title, customerJsonNode, validate);
        return validate;
    }

    private JsonSchema createJsonSchemaByFile() throws IOException {
        InputStream resourceAsStream = ValidatorManager.class.getResourceAsStream(schemaFilePath);
        JsonNode jsonNode = objectMapper.readTree(resourceAsStream);
        SchemaValidatorsConfig schemaValidatorsConfig = new SchemaValidatorsConfig();
        schemaValidatorsConfig.setOpenAPI3StyleDiscriminators(false);
        schemaValidatorsConfig.setFailFast(false);
        JsonSchema jsonSchema = SchemaManager.getSchema(jsonNode, schemaValidatorsConfig);
        return jsonSchema;
    }

    /**
     * logging
     *
     * @param title
     * @param customerJsonNode
     * @param validate
     */
    private static void logging(String title, JsonNode customerJsonNode, Set<ValidationMessage> validate) {
        if (validate.isEmpty()) {
            log.info("{} 验证 start", title);
            log.info(customerJsonNode.toString());
            log.info(validate.toString());
            log.info("{} 验证 {} end", title, validate.isEmpty() ? "成功" : "失败");
        } else {
            log.warn("{} 验证 start", title);
            log.warn(customerJsonNode.toString());
            log.warn(validate.toString());
            log.warn("{} 验证 {} end", title, validate.isEmpty() ? "成功" : "失败");
        }
    }
}
