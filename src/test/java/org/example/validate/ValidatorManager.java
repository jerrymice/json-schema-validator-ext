package org.example.validate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.jerrymice.schema.SchemaManager;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.SchemaValidatorsConfig;
import com.networknt.schema.ValidationMessage;
import lombok.extern.slf4j.Slf4j;
import org.example.validate.model.Customer;

import java.io.InputStream;
import java.util.Set;

@Slf4j
public class ValidatorManager {
    private ObjectMapper objectMapper = new ObjectMapper();

    /**
     * @param customer
     * @param title
     * @return
     * @throws Exception
     */
    public Set<ValidationMessage> validateCustomer(Customer customer, String title) throws Exception {
        InputStream resourceAsStream = ValidatorManager.class.getResourceAsStream("/CustomerSchema.json");
        JsonNode jsonNode = objectMapper.readTree(resourceAsStream);
        SchemaValidatorsConfig schemaValidatorsConfig = new SchemaValidatorsConfig();
        schemaValidatorsConfig.setOpenAPI3StyleDiscriminators(false);
        schemaValidatorsConfig.setFailFast(false);
        JsonSchema jsonSchema = SchemaManager.getSchema(jsonNode, schemaValidatorsConfig);
        String json = objectMapper.writeValueAsString(customer);
        JsonNode customerJsonNode = objectMapper.readTree(json);
        Set<ValidationMessage> validate = jsonSchema.validate(customerJsonNode);
        System.out.println();
        logging(title, customerJsonNode, validate);
        return validate;
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

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }
}
