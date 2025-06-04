package core;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * A JSONObject is a custom data structure that represents a JSON object. It supports nested key-value pairs, arrays (as
 * Lists), and primitive values such as strings, numbers, booleans, and null. This class provides methods for type
 * casting, searching, and string representation.
 */
public class JSONObject implements Iterable<Object> {

    /*
     * Valid JSON types include:
     * String:
     *      "String"
     *      ""
     *      Any characters inside double quotes.
     *      In Java, instance of java.lang.String
     * Number:
     *      10
     *      4.2
     *      1.5E2
     *      Integer or Floating Point number, consisting of digits, at most one decimal place, and possibly E/e followed by an exponent.
     *      In Java, instance of java.lang.Number (Integer, Long, Float, Double)
     * Object:
     *      {"key1" : "value", "key2 : "value"}
     *      {}
     *      A set of key-value pairs separated by commas inside curly braces.
     *      In Java, instance of core.JSONObject
     * Array:
     *      ["value", 10, ...]
     *      []
     *      A list of bare values separated by commas inside square braces.
     *      In Java, instance of java.util.List<?>
     * Boolean:
     *      true
     *      false
     *      Either true or false.
     *      In Java, instance of java.lang.Boolean
     * Null:
     *      null
     *      Represents nothing.
     *      In Java, null
     */

    /**
     * Checks whether an Object value is a valid JSON type: {@code String}, {@code Number}, {@code JSONObject},
     * {@code Array}, {@code Boolean}, or {@code null}.
     *
     * @param value
     *            The Object to check the type of.
     *
     * @return {@code true} if the Object is of a valid JSON type, otherwise {@code false}
     */
    private static boolean isValidJsonType(Object value) {
        return (
            value instanceof String ||
            value instanceof Number ||
            value instanceof JSONObject ||
            value instanceof List<?> ||
            value instanceof Boolean ||
            value instanceof Object
        );
    }

    private String key;
    private Object value;
    private Class<? extends Object> type;

    /**
     * Create an empty JSONObject instance with a key of {@code ""} and a value of {@code null}.
     */
    public JSONObject() {
        this.key = "";
        this.value = null;
        this.type = null;
    }

    /**
     * Create a {@code JSONObject} with the given key and a value of {@code null}.
     *
     * @param key
     *            A {@code String} key
     */
    public JSONObject(String key) {
        this.key = key;
        this.value = null;
        this.type = null;
    }

    /**
     * Create a {@code JSONObject} with the given key and value.
     *
     * @param key
     *            A {@code String} key
     * @param value
     *            A valid JSON value
     */
    public JSONObject(String key, Object value) {
        if (!isValidJsonType(value)) {
            throw new IllegalArgumentException("Invalid JSON value type.");
        }
        this.key = key;
        this.value = value;
        if (value == null)
            type = null;
        else if (value instanceof List<?>)
            if (isJSONList(getAsList()))
                type = JSONObject.class;
            else
                type = ArrayList.class;
        else
            type = value.getClass();
    }

    public JSONObject(String key, Object value, Class<? extends Object> type) {
        this.key = key;
        this.value = value;
        this.type = type;
    }

    private boolean isJSONList(List<?> list) {
        for (Object o : list)
            if (!(o instanceof JSONObject))
                return false;
        return true;
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

    /**
     * Return the value as the given class.
     *
     * @param clazz
     *            An existant class to cast this object's value into.
     *
     * @return The value as a type of the given class, or null if uncastable.
     */
    public <T> T getValueAs(Class<T> clazz) {
        try {
            return clazz.cast(value);
        } catch (ClassCastException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getAsString() {
        return value instanceof String ? (String) value : null;
    }

    public Number getAsNumber() {
        return value instanceof Number ? (Number) value : null;
    }

    @SuppressFBWarnings("NP_BOOLEAN_RETURN_NULL")
    public Boolean getAsBoolean() {
        return value instanceof Boolean ? (Boolean) value : null;
    }

    public JSONObject getAsObject() {

        if (value instanceof JSONObject) return (JSONObject) value;

        if (value instanceof List<?> && type.equals(JSONObject.class)) {
            return this;
        }

        return null;
    }

    public List<?> getAsList() {
        return value instanceof List<?> ? (List<?>) value : null;
    }

    public void setValue(Object value) {
        this.value = value;
        if (value instanceof List<?> && !((List<?>) value).isEmpty()) {
            // For non-empty lists, store the type of the first element
            Object first = ((List<?>) value).get(0);
            if (first instanceof JSONObject) {
                this.type = JSONObject.class;
            } else {
                this.type = value.getClass();
            }
        } else {
            this.type = value != null ? value.getClass() : null;
        }

    }

    public void setValue(Object value, Class<? extends Object> type) {
        this.value = value;
        // Commented out because it is possible to have a mixed list
        // Check that the type is correct
        // if (value instanceof List<?>) {
        //     List<?> list = (List<?>) value;
        //     if (!list.isEmpty()) {
        //         Object first = list.get(0);
        //         if (first.getClass() != type)
        //             throw new InvalidParameterException("The given type, " + type.getName() + ", does not match the actual type of the list entries, which is " + first.getClass().getName());
        //     }
        // }
        this.type = type;
    }

    /** Returns the type of the inner object, even if technically empty or null. */
    public Class<? extends Object> getType() {
        return type;
    }

    /**
     * Search the JSONObject tree structure for a JSONObject with the given key, and return the value of that
     * JSONObject.
     *
     * @param key
     *            A String key to search for within the tree structure.
     *
     * @return The value of the JSONObject with the given key, or null if unfound.
     */
    public Object get(String key) {
        if (this.key.equals(key))
            return this.value;

        if (value instanceof JSONObject)
            return getAsObject().get(key);

        if (value instanceof List<?>) {
            for (Object item : getAsList()) {
                if (item instanceof JSONObject) {
                    Object result = ((JSONObject) item).get(key);
                    if (result != null)
                        return result;
                }
            }
        }
        return null;
    }

    /**
     * Get the native type of the inner value of this JSONObject.
     *
     * @return The class of the value (String, Number, JSONObject, List<?>, Boolean, or null)
     */
    public Class<?> getInnerType() {
        return value.getClass();
    }

    // ITERATOR METHODS -------------------------------------------------------

    /**
     * Traverse the tree structure of this JSONObject inorder and return an indexable list of the objects' values. Start
     * with the leftmost value, then the root value, then the rightmost value, recursively.
     *
     * @param result
     *            A List<Object> to be populated with the values from the inorder traversal.
     */
    public void inorderTraversal(List<Object> result) {
        if (value == null) {
            result.add(null);
        } else if (value instanceof JSONObject) {
            getAsObject().inorderTraversal(result);
        } else if (value instanceof List<?>) {
            for (Object item : getAsList()) {
                if (item instanceof JSONObject) {
                    ((JSONObject) item).inorderTraversal(result);
                } else
                    result.add(item);
            }
        } else
            result.add(value);
    }

    /**
     * Iterate through the tree structure of this JSONObject inorder
     */
    @Override
    public Iterator<Object> iterator() {
        List<Object> traversal = new ArrayList<>();
        inorderTraversal(traversal);
        return traversal.iterator();
    }

    // Stringify methods

    /**
     * Turn this JSONObject into a String representation. Should result in a functionally identical String to the JSON
     * object which was read to create this JSONObject.
     *
     * @return The String representation of this JSONObject
     *
     * @see JSONObject#toString(int)
     */
    @Override
    public String toString() {
        return String.join("\n", JSONStringifier.expandJson(JSONStringifier.stringifyJson(this)));
    }

    @Override
    public int hashCode() {
        return this.toString().hashCode();
    }

    /**
     * Determine whether this JSONObect is equal to the other JSONObject by comparing their String representations.
     *
     * @param other
     *            A JSONObject with which to compare this JSONObject
     *
     * @return True if the String representations are the same, False otherwise
     *
     * @see JSONObject#toString()
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        JSONObject other = (JSONObject) obj;
        return this.toString().equals(other.toString());
    }

}