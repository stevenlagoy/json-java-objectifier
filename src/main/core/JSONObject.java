import java.util.List;

public class JSONObject {

    private String key;
    private Object value;

    public JSONObject() {
        this.key = "";
        this.value = null;
    }

    public JSONObject(String key) {
        this.key = key;
        this.value = null;
    }

    public JSONObject(String key, Object value) {
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

    public Object getValue() {
        return value;
    }
    public <T> T getValueAs(Class<T> clazz) {
        try {
            return clazz.cast(value);
        }
        catch (ClassCastException e) {
            e.printStackTrace();
            return null;
        }
    }
    public void setValue(Object value) {
        this.value = value;
    }
    public String getAsString() {
        return value instanceof String ? (String) value : null;
    }
    public Number getAsNumber() {
        return value instanceof Number ? (Number) value : null;
    }
    public Boolean getAsBoolean() {
        return value instanceof Boolean ? (Boolean) value : null;
    }
    public JSONObject getAsObject() {
        return value instanceof JSONObject ? (JSONObject) value : null;
    }
    public List<?> getAsList() {
        return value instanceof List<?> ? (List<?>) value : null;
    }

    public Class<?> getInnerType() {
        return value.getClass();
    }

    private boolean isValidJsonType(Object value) {
        return (
            value == null ||
            value instanceof String ||
            value instanceof Number ||
            value instanceof Boolean ||
            value instanceof JSONObject ||
            value instanceof List<?>
        );
    }

    public String toString() {
        return toString(0);
    }
    
    private String toString(int indent) {
        StringBuilder sb = new StringBuilder();
        String indentation = "    ".repeat(indent);
        
        sb.append(String.format("\"%s\" : ", this.key));
        
        if (value == null) {
            sb.append("null");
        } else if (value instanceof String) {
            sb.append(String.format("\"%s\"", value));
        } else if (value instanceof Number || value instanceof Boolean) {
            sb.append(value);
        } else if (value instanceof List<?>) {
            List<?> list = (List<?>) value;
            if (list.isEmpty()) {
                sb.append("{}");
            } else {
                sb.append("{\n");
                for (int i = 0; i < list.size(); i++) {
                    sb.append(indentation).append("    ");
                    Object item = list.get(i);
                    if (item instanceof JSONObject) {
                        // Don't add extra braces for nested objects in arrays
                        sb.append(((JSONObject) item).toString(indent + 1));
                    } else {
                        sb.append(formatValue(item));
                    }
                    if (i < list.size() - 1) {
                        sb.append(",");
                    }
                    sb.append("\n");
                }
                sb.append(indentation).append("}");
            }
        } else if (value instanceof JSONObject) {
            JSONObject obj = (JSONObject) value;
            sb.append("{\n");
            sb.append(indentation).append("    ").append(obj.toString(indent + 1));
            sb.append("\n").append(indentation).append("}");
        }
        
        return sb.toString();
    }
    
    private String formatValue(Object val) {
        if (val == null) return "null";
        if (val instanceof String) return String.format("\"%s\"", val);
        return val.toString();
    }

    public boolean equals(JSONObject other) {
        return this.toString().equals(other.toString());
    }
}