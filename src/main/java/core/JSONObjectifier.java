package core;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class JSONObjectifier {

    /**
     * Turns a JSON String List representation into a nested JSONObject. 
     * @param JSONContents A List of Strings representing the JSON file
     * @return A nested JSONObject parsed from the read JSON file, or null if the file is not valid JSON format
     * @see Objectifier#objectify(String, List)
     * @see Objectifier#verifyJSONFile(path)
     */
    public static JSONObject objectify(Path path) {
        if (!JSONValidator.validateJson(path)) return null;
        List<String> JSONContents = JSONReader.readLines(path);
        String name = path.getFileName().toString().split("\\.")[0];
        return objectify(name, JSONContents);
    }

    /**
     * Turns a JSON String List representation into a nested JSONObject.
     * @param objectName A String to be used as the key for the returned JSONObject 
     * @param JSONContents A List of Strings representing the JSON file
     * @return A JSONObject with the given key and nested values parsed from the List of JSON lines, or null if the JSONContents are not valid JSON format
     * @see Objectifier#verifyJSONFile(List)
     */
    public static JSONObject objectify(String objectName, List<String> JSONContents) {
        // Partial JSON files are often passed into this object - their validity should not be checked
        // if (!verifyJSONFile(JSONContents)) return null; // Validate format before parsing

        JSONObject root = new JSONObject(objectName);
        Class<? extends Object> innerType = JSONObject.class; // Assume any nested entries will be of type JSONObject
        List<JSONObject> children = new ArrayList<>();

        for (int i = 0; i < JSONContents.size(); i++) {
            String line = JSONContents.get(i).trim();

            // Skip structural or empty lines
            if (line.isEmpty() || line.equals("{") || line.equals("}") || line.equals("},"))
                continue;
            
            String key = "", valueString = "";
            try {
                // Split the line at the first unqioted colon
                String[] split = StringOperations.splitByUnquotedString(line, ":", 2);
                key = split[0].trim().replaceAll("^\"|\"$", "");
                valueString = split.length > 1 ? split[1].trim() : "";
            }
            catch (ArrayIndexOutOfBoundsException e) {
                continue;  // expected when the line is empty, contains just a key, just a colon, just a bracket, etc 
            }

            if (key.equals("single_line_empty_array"))
                System.out.println("single_line_empty_array");

            // Start of a nested object (inline or multi-line)
            if (valueString.startsWith("{")) {
                int opens = StringOperations.countUnquotedChar(valueString, '{');
                int closes = StringOperations.countUnquotedChar(valueString, '}');

                // Inline object on a single line
                if (opens <= closes) {
                    String inner = valueString.substring(1, valueString.length() - 1).trim();
                    List<String> mini = new ArrayList<>();
                    mini.add("{");
                    if (!inner.isEmpty()) {
                        String[] parts = StringOperations.splitByStringNotNested(inner, ",");
                        for (int j = 0; j < parts.length; j++) {
                            String lineEntry = parts[j].trim();
                            if (j < parts.length - 1) lineEntry += ",";
                            mini.add(lineEntry);
                        }
                    }
                    mini.set(mini.size() - 1, StringOperations.replaceLast(mini.get(mini.size() - 1), "}", ""));
                    mini.add("}");
                    children.add(objectify(key, mini));
                }
                else {
                    String inner = valueString.substring(1).trim();
                    int depth = opens - closes;
                    List<String> sublines = new ArrayList<>();
                    if (!inner.isEmpty()) sublines.add(inner);
                    while (++i < JSONContents.size() && depth > 0) {
                        String nestedLine = JSONContents.get(i);
                        depth += StringOperations.countUnquotedChar(nestedLine, '{');
                        depth -= StringOperations.countUnquotedChar(nestedLine, '}');
                        if (depth > 0) sublines.add(nestedLine);
                    }
                    if (!inner.isEmpty()) sublines.add("}");
                    i--;
                    children.add(objectify(key, sublines));
                }
            }

            // Start of an array
            else if (valueString.startsWith("[")) {
                innerType = ArrayList.class;
                List<String> arrayLines = new ArrayList<>();
                int depth = 0;
            
                // Add first line and update depth
                arrayLines.add(valueString);
                depth += StringOperations.countUnquotedChar(valueString, '[');
                depth -= StringOperations.countUnquotedChar(valueString, ']');
            
                // Collect all lines until the array closes
                while (depth > 0 && ++i < JSONContents.size()) {
                    String arrayLine = JSONContents.get(i).trim();
                    arrayLines.add(arrayLine);
                    depth += StringOperations.countUnquotedChar(arrayLine, '[');
                    depth -= StringOperations.countUnquotedChar(arrayLine, ']');
                }
                
                // Reconstruct array into one line
                String fullArray = String.join(" ", arrayLines).trim();

                @SuppressWarnings("unchecked")
                List<Object> parsedArray = (List<Object>) parseValue(fullArray);

                children.add(new JSONObject(key, parsedArray, Object.class));
            }
            
            // Empty objects or arrays
            else if (valueString.equals("{"))
                children.add(new JSONObject(key, new JSONObject(key), JSONObject.class));
            else if (valueString.equals("["))
                children.add(new JSONObject(key, new ArrayList<>(), Object.class));
            
                // Primitive value
            else {
                Object parsed = parseValue(valueString);
                children.add(new JSONObject(key, parsed));
            }
        }

        root.setValue(children, innerType);
        return root;
    }

    /**
     * Interpret the value as a Java type.
     * <ul>
     *   <li>String  -> java.lang.String
     *   <li>Number  -> java.lang.Number
     *   <li>Object  -> core.JSONObject
     *   <li>Array   -> java.util.List&lt;?&gt;
     *   <li>Boolean -> java.lang.Boolean
     *   <li>null    -> null
     * </ul>
     * Create an Object of the associated type with the equivalent value.
     * @param val The String form of a valid JSON value
     * @return An Object of the Java type matched to the JSON value's type. Returns the passed String if not mapped to another type.
     */
    private static Object parseValue(String val) {
        val = val.replaceAll(",$", "").trim();

        // Quoted strings
        if (val.startsWith("\"") && val.endsWith("\""))
            return val.substring(1, val.length() - 1);

        // Null values
        if (val.equals("null"))
            return null;

        // Boolean values
        if (val.equals("true") || val.equals("false"))
            return Boolean.parseBoolean(val);

        // JSON Objects
        if (val.startsWith("{") && val.endsWith("}")) {
            return objectify("", List.of(val.substring(1, val.length()-1)));
        }

        // Arrays
        if (val.startsWith("[") && val.endsWith("]")) {
            val = val.substring(1, val.length() - 1).trim();
            List<Object> list = new ArrayList<>();
            if (!val.isEmpty()) {
                String[] split = StringOperations.splitByStringNotInArray(val, ",");
                for (String element : split) {
                    list.add(parseValue(element));
                }
            }
            return list;
        }

        // Integer values
        try {
            return Integer.parseInt(val);
        }
        catch (NumberFormatException e1) {}

        // Long values
        try {
            return Long.parseLong(val);
        }
        catch (NumberFormatException e2) {}

        // Double values
        try {
            Double d = Double.parseDouble(val); 
            if (Double.isFinite(d)) return d;
        }
        catch (NumberFormatException e3) {}

        // Fallback with original value
        return val;
    }

}
