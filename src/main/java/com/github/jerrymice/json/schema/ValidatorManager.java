package com.github.jerrymice.json.schema;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.SchemaValidatorsConfig;
import com.networknt.schema.ValidationMessage;
import com.networknt.schema.ValidationResult;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public class ValidatorManager {

    private ObjectMapper objectMapper = new ObjectMapper();

    private String schemaFilePath;

    private JsonSchema jsonSchema;

    private SchemaValidatorsConfig schemaValidatorsConfig;

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

    public SchemaValidatorsConfig getSchemaValidatorsConfig() {
        return schemaValidatorsConfig;
    }

    public void setSchemaValidatorsConfig(SchemaValidatorsConfig schemaValidatorsConfig) {
        this.schemaValidatorsConfig = schemaValidatorsConfig;
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

    public ValidationResult walk(Object javabean, String title, boolean shouldValidateSchema) throws Exception {
        initJsonSchema();
        return walk(javabean, title, jsonSchema, shouldValidateSchema);
    }

    public void initJsonSchema() throws Exception {
        if (schemaValidatorsConfig == null) {
            schemaValidatorsConfig = createDefaultSchemaValidatorsConfig();
        }
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
    private ValidationResult walk(Object javabean, String title, JsonSchema jsonSchema, boolean shouldValidateSchema) throws Exception {
        String json = objectMapper.writeValueAsString(javabean);
        JsonNode customerJsonNode = objectMapper.readTree(json);
        ValidationResult result = jsonSchema.walk(customerJsonNode, true);
        handleValidationMessage(result);
        logging(title, customerJsonNode, result.getValidationMessages());
        return result;
    }

    private void handleValidationMessage(ValidationResult result) {
        Set<ValidationMessage> validationMessages = result.getValidationMessages();
        Iterator<ValidationMessage> iterator = validationMessages.iterator();
        Set<String> newMessageUnionIdSet = validationMessages.stream().filter(i -> !i.getMessage().startsWith("$.")).map(this::getMessageUnionId).collect(Collectors.toSet());
        while (iterator.hasNext()) {
            ValidationMessage next = iterator.next();
            String unionId = getMessageUnionId(next);
            if (next.getMessage().startsWith("$.") && newMessageUnionIdSet.contains(unionId)) {
                iterator.remove();
            }
        }
    }

    private String getMessageUnionId(ValidationMessage next) {
        String args = next.getArguments() != null && next.getArguments().length > 0 ? next.getArguments()[0] : "";
        String unionId = next.getType() + "/" + next.getSchemaPath() + "/" + next.getPath() + "/" + args;
        return unionId;
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
        JsonSchema jsonSchema = SchemaManager.getSchema(jsonNode, this.schemaValidatorsConfig);
        return jsonSchema;
    }

    public SchemaValidatorsConfig createDefaultSchemaValidatorsConfig() {
        SchemaValidatorsConfig schemaValidatorsConfig = new SchemaValidatorsConfig();
        schemaValidatorsConfig.setOpenAPI3StyleDiscriminators(false);
        schemaValidatorsConfig.setFailFast(false);
        return schemaValidatorsConfig;
    }

    /**
     * logging
     *
     * @param title
     * @param customerJsonNode
     * @param validate
     */
    private static void logging(String title, JsonNode customerJsonNode, Set<ValidationMessage> validate) {
        System.out.println();
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
