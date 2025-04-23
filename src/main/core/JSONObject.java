package src.main.core;

import java.util.Collection;
import java.util.List;

public class JSONObject <T> {

    private String key;
    private T value;

    public JSONObject(String key, T value) {
        if (!isValidJsonType(value)) {
            throw new IllegalArgumentException("Invalid JSON value type.");
        }
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }
    public void setKey(String key) {
        this.key = key;
    }

    public T getValue() {
        return value;
    }
    public void setValue(T value) {
        this.value = value;
    }

    private boolean isValidJsonType(Object value) {
        return (
            value == null ||
            value instanceof String ||
            value instanceof Number ||
            value instanceof Boolean ||
            value instanceof JSONObject<?> ||
            value instanceof List<?>
        );
    }
}