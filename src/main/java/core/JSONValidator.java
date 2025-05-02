package core;

import java.nio.file.Path;
import java.util.EmptyStackException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class JSONValidator {
    
    /**
     * Verifies that a JSON file is properly formatted. Keys must be unique strings, objects must be comma-separated, braces must be open-close matched.
     * @param filepath The path to a JSON file
     * @return True if the file is properly formatted, False otherwise
     * @see Objectifier#verifyJSONFile(List)
     */
    public static boolean validate(Path filepath) {
        List<String> contents = JSONReader.readLines(filepath);
        return validate(contents);
    }
    /**
     * Verifies that a JSON String representation is properly formatted. Keys must be unique strings, objects must be comma-separated, braces must be open-close matched.
     * @param contents A List of Strings representing the JSON file
     * @return True if the String representation is properly formatted, False otherwise
     */
    public static boolean validate(List<String> contents) {
        if (contents.size() == 0)
            return false; // JSON files must hold at least one object, so empty files are invalid.
        Stack<Integer> stack = new Stack<>();
        Stack<Character> structureStack = new Stack<>();
        Map<Integer, Integer> indices = new HashMap<>();
        boolean firstObjectEntry = false;

        for (int i = 0; i < contents.size(); i++) {
            String line = contents.get(i).trim();
            if (line == null || line.isEmpty()) continue;
            boolean hasStructuralChars = false;

            // Opening structural characters
            if (!hasStructuralChars && (StringOperations.containsUnquotedChar(line, '{') || StringOperations.containsUnquotedChar(line, '['))) {
                hasStructuralChars = true;
                int openBraces = StringOperations.countUnquotedChar(line, '{');
                int closeBraces = StringOperations.countUnquotedChar(line, '}');
                int openBrackets = StringOperations.countUnquotedChar(line, '[');
                int closeBrackets = StringOperations.countUnquotedChar(line, ']');

                // Must be in this order: Open Braces, Close Braces, Open Brackets, Close Brackets

                for (int j = 0; j < openBraces; j++) {
                    stack.push(i);
                    structureStack.push('{');
                    firstObjectEntry = true;
                }

                for (int j = 0; j < closeBraces; j++) {
                    try {
                        indices.put(stack.pop(), i);
                        if (!structureStack.isEmpty() && structureStack.peek() == '{') {
                            structureStack.pop();
                        }
                        else return false; // Mismatched braces
                    }
                    catch (EmptyStackException e) {
                        return false;
                    }
                }

                for (int j = 0; j < openBrackets; j++) {
                    structureStack.push('[');
                }

                for (int j = 0; j < closeBrackets; j++) {
                    if (!structureStack.isEmpty() && structureStack.peek() == '[') {
                        structureStack.pop();
                    }
                    else return false; // Mismatched brackets
                }
            }

            // Closing structural characters
            if (!hasStructuralChars && (StringOperations.containsUnquotedChar(line, '}') || StringOperations.containsUnquotedChar(line, ']'))) {
                hasStructuralChars = true;
                int closeBraces = StringOperations.countUnquotedChar(line, '}');
                int closeBrackets = StringOperations.countUnquotedChar(line, ']');

                for (int j = 0; j < closeBraces; j++) {
                    try {
                        indices.put(stack.pop(), i);
                        if (!structureStack.isEmpty() && structureStack.peek() == '{') {
                            structureStack.pop();
                        }
                        else return false; // Mismatched braces
                    }
                    catch (EmptyStackException e) {
                        return false;
                    }
                }

                for (int j = 0; j < closeBrackets; j++) {
                    if (!structureStack.isEmpty() && structureStack.peek() == '[') {
                        structureStack.pop();
                    }
                    else return false; // Mismatched brackets
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
            // The element has a comma: check that it's supposed to be there.
            if (hasComma) {
                // An element should have a comma if it is not the last element in an array or list
                if (nextLine.startsWith("}") || nextLine.startsWith("]")) {
                    return false; // Misplaced comma afer last entry
                }
            }
            // The element does not have a comma: check that it's not supposed to.
            else {
                // The element should not have a comma if it is the last element in an array or list
                if (nextLine != null && !nextLine.startsWith("}") && !nextLine.startsWith("]")) {
                    if (!line.endsWith("{") && !line.endsWith("[")) return false; // Missing comma between entries
                }
            }

            if (hasStructuralChars) continue;

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

            if (!hasStructuralChars)
                firstObjectEntry = false;
        }
        return stack.isEmpty() && structureStack.isEmpty();
        // If false, there is an opening brace/bracket unmatched to a closing brace/bracket.
        // If true, no errors encountered: the JSON file is well-structured.
    }

}
