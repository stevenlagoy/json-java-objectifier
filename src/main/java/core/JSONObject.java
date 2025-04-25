package core;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class JSONObject implements Iterable<Object> {

    public static final int SINGLE_LINE_ENTRIES_MAX = 5; // The maximum number of array entries which may be put on a single line in the string representation. Objects are expanded and count as three lines.
    public static final int LINE_LENGTH_MAX = 55; // The maximum length of a line in string representation. An attempt will be made to split longer lines.
    public static final String INDENT = "    "; // The string that will be used as indentation.

    private static boolean isValidJsonType(Object value) {
        return (
            value == null ||
            value instanceof String ||
            value instanceof Number ||
            value instanceof Boolean ||
            value instanceof JSONObject ||
            value instanceof List<?>
        );
    }

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
    public Object get(String key) {
        if (this.key.equals(key))
            return this.value;

        if (value instanceof JSONObject)
            return getAsObject().get(key);
        
        if (value instanceof List<?>) {
            for (Object item : getAsList()) {
                if (item instanceof JSONObject) {
                    Object result = ((JSONObject) item).get(key);
                    if (result != null) return result;
                }
            }
        }
        return null;
    }

    public Class<?> getInnerType() {
        return value.getClass();
    }

    // Iterator methods
    public void inorderTraversal(List<Object> result) {
        if (value == null) {
            result.add(null);
        }
        else if (value instanceof JSONObject) {
            getAsObject().inorderTraversal(result);
        }
        else if (value instanceof List<?>) {
            for (Object item : getAsList()) {
                if (item instanceof JSONObject) {
                    ((JSONObject) item).inorderTraversal(result);
                }
                else result.add(item);
            }
        }
        else result.add(value);
    }

    @Override
    public Iterator<Object> iterator() {
        List<Object> traversal = new ArrayList<>();
        inorderTraversal(traversal);
        return traversal.iterator();
    }

    // Stringify methods
    public String toString() {
        return toString(0);
    }
    
    private String toString(int indent) {
        StringBuilder sb = new StringBuilder();
        String indentation = INDENT.repeat(indent);
        
        if (!key.equals(""))
            sb.append(String.format("\"%s\" : ", this.key));
        else
            System.out.println("no key");
        
        if (value == null)
            sb.append("null");
        else if (value instanceof String)
            sb.append(String.format("\"%s\"", value));
        else if (value instanceof Number || value instanceof Boolean)
            sb.append(value);
        else if (value instanceof List<?>) {
            List<?> list = getAsList();
            if (list.isEmpty()) {
                sb.append("[]");
            }
            else if (list.get(0) instanceof JSONObject) {
                sb.append("{\n");
                for (int i = 0; i < list.size(); i++) {
                    sb.append(indentation).append(INDENT);
                    JSONObject item = (JSONObject) list.get(i);
                    sb.append(item.toString(indent + 1));
                    if (i < list.size() - 1) sb.append(",");
                    sb.append("\n");
                }
                sb.append(indentation).append("}");
            }
            else {
                sb.append("[\n");
                StringBuilder listStrB = new StringBuilder();
                listStrB.append("[");
                for (int i = 0; i < list.size(); i++) {
                    if (i > 0) listStrB.append(", ");
                    listStrB.append(formatValue(list.get(i)));
                }
                listStrB.append("]");
                String result = listStrB.toString();
        
                if (result.length() > LINE_LENGTH_MAX || countInnerListEntries(list) > SINGLE_LINE_ENTRIES_MAX) {
                    // List must be single entry per line
                    String[] lines = StringOperations.splitByStringNotInArray(result.substring(1, result.length()-1), ",");
                    result = "";
                    for (int i = 0; i < lines.length; i++) {
                        String[] linelines = lines[i].split("\n");
                        int extraIndent = 0;
                        for (int j = 0; j < linelines.length; j++) {
                            String lineline = linelines[j];
                            if (lineline.equals("}")) extraIndent--;
                            result += indentation + INDENT + INDENT.repeat(extraIndent) + lineline.trim();
                            if (lineline.equals("{")) {
                                extraIndent++;
                                result += "\n"; // skip comma
                                continue;
                            }
                            if (j < linelines.length - 2)
                                result += ",\n";
                            else if (j == linelines.length - 2)
                                result += "\n";
                            else if (i < lines.length - 1)
                                result += ",\n";
                        }
                    }
                    // result = String.join(",\n", lines);
                }
                else result = indentation + INDENT + result;

                sb.append(result);
                sb.append("\n").append(indentation).append("]");
            }
        }
        
        return sb.toString();
    }
    
    private String formatValue(Object val) {
        if (val == null) return "null";
        if (val instanceof String) return String.format("\"%s\"", val);
        if (val instanceof JSONObject) return val.toString();
        return val.toString();
    }

    private int countInnerListEntries(List<?> list) {
        int entries = 0;
        for (Object o : list) {
            if (o instanceof JSONObject) entries += 3;
            else if (o instanceof List<?>)
                entries += countInnerListEntries((List<?>) o);
            else entries += 1;
        }
        return entries;
    }

    public boolean equals(JSONObject other) {
        return this.toString().equals(other.toString());
    }
}