package core;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Stack;

public class Objectifier {

    /**
     * Reads a JSON file and returns its contents as a List of Strings.
     * @param filepath The path to a JSON file
     * @return A List of Strings where each entry is a line from the file
     */
    public static List<String> readJSONLines(Path filepath) {
        ArrayList<String> contents = new ArrayList<String>();
        try {
            //String content = new String(Files.readAllBytes(filepath), StandardCharsets.UTF_8);
            try(Scanner scanner = FileOperations.ScannerUtil.createScanner(filepath.toFile())) {
                while (scanner.hasNextLine()) {
                    contents.add(scanner.nextLine());
                }
            }
            return contents;
        }
        catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Verifies that a JSON file is properly formatted. Keys must be unique strings, objects must be comma-separated, braces must be open-close matched.
     * @param filepath The path to a JSON file
     * @return True if the file is properly formatted, False otherwise
     * @see Objectifier#verifyJSONFile(List)
     */
    public static boolean verifyJSONFile(Path filepath) {
        List<String> contents = readJSONLines(filepath);
        return verifyJSONFile(contents);
    }
    /**
     * Verifies that a JSON String representation is properly formatted. Keys must be unique strings, objects must be comma-separated, braces must be open-close matched.
     * @param contents A List of Strings representing the JSON file
     * @return True if the String representation is properly formatted, False otherwise
     */
    public static boolean verifyJSONFile(List<String> contents) {
        if (contents.size() == 0)
            return false; // JSON files must hold at least one object, so empty files are invalid.
        Stack<Integer> stack = new Stack<>();
        Stack<Character> structureStack = new Stack<>();
        Map<Integer, Integer> indices = new HashMap<>();
        boolean firstObjectEntry = false;

        for (int i = 0; i < contents.size(); i++) {
            String line = contents.get(i).trim();
            if (line == null || line.isEmpty()) continue;

            // Opening structural characters
            if (StringOperations.containsUnquotedChar(line, '{') || StringOperations.containsUnquotedChar(line, '[')) {
                if (StringOperations.containsUnquotedChar(line, '{')) {
                    stack.push(i);
                    structureStack.push('{');
                    firstObjectEntry = true;
                }
                if (StringOperations.containsUnquotedChar(line, '[')) {
                    structureStack.push('[');
                }
                continue;
            }

            // Closing structural characters
            if (StringOperations.containsUnquotedChar(line, '}') || StringOperations.containsUnquotedChar(line, ']')) {
                if (StringOperations.containsUnquotedChar(line, '}')) {
                    try {
                        indices.put(stack.pop(), i);
                        if (!structureStack.isEmpty() && structureStack.peek() == '{') {
                            structureStack.pop();
                        }
                        else
                            return false; // Mismatched braces
                    }
                    catch (EmptyStackException e) {
                        return false;
                    }
                }
                if (StringOperations.containsUnquotedChar(line, ']')) {
                    if (!structureStack.isEmpty() && structureStack.peek() == '[') {
                        structureStack.pop();
                    }
                    else
                        return false; // Mismatched braces
                }
                continue;
            }

            // Check format based on current context
            if (!structureStack.isEmpty() && structureStack.peek() == '{') {
                // Inside an object
                if (!StringOperations.containsUnquotedChar(line, ':')) {
                    return false; // Objects must have a key-value pair
                }
                // Validate key format
                String[] parts = StringOperations.splitByUnquotedString(line, ":", 2);
                String key = parts[0].trim();
                if (!key.startsWith("\"") || !key.endsWith("\"")) {
                    return false; // The key must be quoted
                }
            }
            
            // Check for missing or misplaced commas
            boolean hasComma = line.endsWith(",");
            String nextLine = null;
            for (int j = i + 1; j < contents.size(); j++) {
                String next = contents.get(j).trim();
                if (!next.isEmpty()) {
                    nextLine = next;
                    break;
                }
            }
            if (firstObjectEntry) {
                if (hasComma && (nextLine == null || nextLine.equals("}"))) {
                    return false; // Trailing comma after last entry is invalid
                }
            }
            else {
                if (!line.endsWith("{") && !hasComma && nextLine != null && !(nextLine.equals("}") || nextLine.equals("},"))) {
                    return false; // Missing comma between entries
                }
            }

            firstObjectEntry = false;
        }
        return stack.isEmpty() && structureStack.isEmpty();
        // If false, there is an opening brace/bracket unmatched to a closing brace/bracket.
        // If true, no errors encountered: the JSON file is well-structured.
    }

    /**
     * Turns a JSON String List representation into a nested JSONObject. 
     * @param JSONContents A List of Strings representing the JSON file
     * @return A nested JSONObject parsed from the read JSON file, or null if the file is not valid JSON format
     * @see Objectifier#objectify(String, List)
     * @see Objectifier#verifyJSONFile(path)
     */
    public static JSONObject objectify(Path path) {
        List<String> JSONContents = readJSONLines(path);
        String name = path.getFileName().toString().split("\\.")[0];
        if (!verifyJSONFile(JSONContents)) return null;
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
     * <ul><p>
     * String -> java.lang.String
     * <p>
     * Number -> java.lang.Number
     * <p>
     * Object -> core.JSONObject
     * <p>
     * Array -> java.util.List&lt;?&gt;
     * <p>
     * Boolean -> java.lang.Boolean
     * <p>
     * null -> null
     * </ul>
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
