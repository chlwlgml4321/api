package kr.co.hectofinancial.mps.global.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class JsonUtil {

    private final ObjectMapper objectMapper;

    public String toJson(Object src) {
        try {
            return objectMapper.writeValueAsString(src);
        } catch (Exception ex) {
            log.error("to json error={}", ex.getMessage(), ex);
            return null;
        }
    }

    public String toJsonWithoutNull(Object src) {
        try {
            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            return objectMapper.writeValueAsString(src);
        } catch (Exception ex) {
            log.error("to json error={}", ex.getMessage(), ex);
            return null;
        }
    }

    public <T> T fromJson(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (Exception ex) {
            log.error("from json error={}", ex.getMessage(), ex);
            return null;
        }
    }

    public <T> T fromJsonArray(String jsonArray, TypeReference<T> typeReference) throws JsonProcessingException {
        return objectMapper.readValue(jsonArray, typeReference);
    }

    public boolean isJsonFormat(String json) {
        try {
            JsonNode jsonNode = objectMapper.readTree(json);
            return !jsonNode.isTextual();
        } catch (Exception ex) {
            return false;
        }
    }

    public boolean isJsonArray(String json) {
        try {
            JsonNode jsonNode = objectMapper.readTree(json);
            return jsonNode.isArray();
        } catch (Exception ex) {
            return false;
        }
    }

    public <T> T convertValue(Object obj, Class<T> clazz) {
        return objectMapper.convertValue(obj, clazz);
    }

    public <T> T convertValue(Object obj, TypeReference<T> typeReference) {
        return objectMapper.convertValue(obj, typeReference);
    }

    public String toPrettyPrint(Object obj) {
        try {
            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
        } catch (Exception ex) {
            log.error("to pretty print json error={}", ex.getMessage(), ex);
            return null;
        }
    }
}
