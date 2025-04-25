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
     * @param filepath The path to a JSON file.
     * @return A List of Strings where each entry is a line from the file.
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
     * @param filepath The path to a JSON file.
     * @return True if the file is properly formatted, False otherwise.
     */
    public static boolean verifyJSONFile(Path filepath) {
        List<String> contents = readJSONLines(filepath);
        return verifyJSONFile(contents);
    }
    /**
     * Verifies that a JSON String representation is properly formatted. Keys must be unique strings, objects must be comma-separated, braces must be open-close matched.
     * @param contents A List of Strings representing the JSON file.
     * @return True if the String representation is properly formatted, False otherwise.
     */
    public static boolean verifyJSONFile(List<String> contents) {
        if (contents.size() == 0) return false; // JSON files must hold at least one object, so empty files are invalid.
        Stack<Integer> stack = new Stack<>();
        Map<Integer, Integer> indices = new HashMap<>();
        for (int i = 0; i < contents.size(); i++) {
            String line = contents.get(i);
            if (line == null) break;
            if (StringOperations.containsUnquotedChar(line, '{')) {
                stack.push(i);
            }
            if (StringOperations.containsUnquotedChar(line, '}')) {
                try {
                    indices.put(stack.pop(), Integer.valueOf(i));
                }
                catch (EmptyStackException e) {
                    return false; // There is no opening brace to which this closing brace can be matched.
                }
            }
        }
        if (!stack.isEmpty()) return false; // There is an opening brace unmatched to a closing brace.
        
        return true; // No errors encountered: the JSON file is well-structured.
    }

    public static JSONObject objectify(Path path) {
        List<String> JSONContents = readJSONLines(path);
        String name = path.getFileName().toString().split("\\.")[0];
        if (!verifyJSONFile(JSONContents)) return null;
        return objectify(name, JSONContents);
    }

    /**
     * Turns a JSON String List representation into a nested JSONObject. 
     * @param JSONContents A List of Strings representing the JSON file.
     * @return 
     */
    public static JSONObject objectify(String objectName, List<String> JSONContents) {
        JSONObject root = new JSONObject(objectName);
        List<JSONObject> children = new ArrayList<>();

        for (int i = 0; i < JSONContents.size(); i++) {
            String line = JSONContents.get(i).trim();
            if (line.isEmpty() || line.equals("{") || line.equals("}") || line.equals("},"))
                continue;
            
            String key = "", valueString = "";
            try {
                String[] split = StringOperations.splitByUnquotedString(line, ":", 2);
                key = split[0].trim().replaceAll("^\"|\"$", "");
                valueString = split.length > 1 ? split[1].trim() : "";
            }
            catch (ArrayIndexOutOfBoundsException e) {
                continue;  // expected when the line is empty, contains just a key, just a colon, just a bracket, etc 
            }

            if (key.equals("single_line_array"))
                System.out.println("single_line_array");

            if (valueString.startsWith("{")) {
                int depth = 1;
                List<String> subLines = new ArrayList<>();
                while (++i < JSONContents.size() && depth > 0) {
                    String nestedLine = JSONContents.get(i);
                    if (StringOperations.containsUnquotedChar(nestedLine, '{')) depth++;
                    if (StringOperations.containsUnquotedChar(nestedLine, '}')) {
                        depth--;
                        if (depth == 0) {
                            i--;
                            break;
                        }
                    }
                    subLines.add(nestedLine);
                }
                children.add(objectify(key, subLines));
            }
            else if (valueString.startsWith("[")) {
                List<String> arrayLines = new ArrayList<>();
                int depth = 0;
            
                // Add first line and update depth
                arrayLines.add(valueString);
                depth += StringOperations.countUnquotedChar(valueString, '[');
                depth -= StringOperations.countUnquotedChar(valueString, ']');
            
                // Collect all lines of the array
                while (depth > 0 && ++i < JSONContents.size()) {
                    String arrayLine = JSONContents.get(i).trim();
                    arrayLines.add(arrayLine);
                    depth += StringOperations.countUnquotedChar(arrayLine, '[');
                    depth -= StringOperations.countUnquotedChar(arrayLine, ']');
                }
                
                String fullArray = String.join(" ", arrayLines).trim();

                @SuppressWarnings("unchecked")
                List<Object> parsedArray = (List<Object>) parseValue(fullArray);

                children.add(new JSONObject(key, parsedArray));
            }
            else if (valueString.equals("{"))
                children.add(new JSONObject(key, new JSONObject(key)));
            else if (valueString.equals("["))
                children.add(new JSONObject(key, new ArrayList<>()));
            else {
                Object parsed = parseValue(valueString);
                children.add(new JSONObject(key, parsed));
            }
        }
        root.setValue(children);
        return root;
    }

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
