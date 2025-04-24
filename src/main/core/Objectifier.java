import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.Stack;
import java.io.File;

public class Objectifier {

    public static void main(String[] args) {

        System.setProperty("file.encoding", "UTF-8");
        System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));

        try {
            Set<Path> paths = FileOperations.listFiles(FilePaths.DATA_SOURCE, FileOperations.FileExtension.JSON);
            List<JSONObject> JSONs = new ArrayList<>();
            for (Path path : paths) {
                JSONs.add(objectify(path));
            }
            try (FileWriter fw = new FileWriter(new File("data/result.out"))) {
                for (JSONObject JSON : JSONs) fw.write(JSON.toString());
            }
            
        }
        catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

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
            if (line.isEmpty() || line.equals("{") || line.equals("}")) continue;
            
            String key = "", valueString = "";
            try {
                String[] split = StringOperations.splitByUnquotedString(line, ":", 2);
                key = split[0].trim().replaceAll("^\"|\"$", "");
                valueString = split.length > 1 ? split[1].trim() : "";
            }
            catch (ArrayIndexOutOfBoundsException e) {
                continue;  // expected when the line is empty, contains just a key, just a colon, just a bracket, etc 
            }

            if (valueString.startsWith("{")) {
                int start = i + 1, depth = 1;
                List<String> subLines = new ArrayList<>();
                while (++i < JSONContents.size() && depth > 0) {
                    String nestedLine = JSONContents.get(i);
                    if (StringOperations.containsUnquotedChar(nestedLine, '{')) depth++;
                    if (StringOperations.containsUnquotedChar(nestedLine, '}')) depth--;
                    if (depth > 0) subLines.add(nestedLine);
                }
                children.add(objectify(key, subLines));
            }
            else if (valueString.startsWith("[")) {
                List<Object> array = new ArrayList<>();
                if (valueString.endsWith("]")) {
                    String inner = valueString.substring(1, valueString.length() - 1).trim();
                    if (!inner.isEmpty()) {
                        String[] entries = StringOperations.splitByUnquotedString(inner, ",");
                        for (String entry : entries) {
                            array.add(parseValue(entry.trim()));
                        }
                    }
                }
                else {
                    int depth = 1;
                    List<String> subLines = new ArrayList<>();
                    while (++i < JSONContents.size() && depth > 0) {
                        String arrayLine = JSONContents.get(i).trim();
                        if (StringOperations.containsUnquotedChar(arrayLine, '[')) depth++;
                        if (StringOperations.containsUnquotedChar(arrayLine, ']')) depth--;
                        if (depth > 0) subLines.add(arrayLine);
                    }
                    for (String entry : subLines) {
                        array.add(parseValue(entry));
                    }
                }
                children.add(new JSONObject(key, array));
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
        if (val.startsWith("\"") && val.endsWith("\""))
            return val.substring(1, val.length() - 1);
        if (val.equals("null"))
            return null;
        if (val.equals("true") || val.equals("false"))
            return Boolean.parseBoolean(val);
        try {
            return Integer.parseInt(val);
        }
        catch (NumberFormatException e1) {}
        try {
            return Double.parseDouble(val); 
        }
        catch (NumberFormatException e2) {}
        return val;
    }

}
