package core;

import java.nio.file.Path;
import java.util.EmptyStackException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;

public class JSONValidator {
    
    /*
     * JSON BNF Grammar
     * <object>     ::= { } | { <members> }
     * <members>    ::= <pair> | <pair> , <members>
     * <pair>       ::= <string> : <value>
     *                  semantics rule: <string> must be unique within its level
     * 
     * <array>      ::= [ ] | [ <elements> ]
     * <elements>   ::= <value> | <value> , <elements>
     * 
     * <value>      ::= <string> | <number> | <object> | <array> | true | false | null
     * 
     * <string>     ::= " <characters> "
     * <characters> ::= <character> | <character> <characters>
     * <character>  ::= # any unicode character except " or \ or control characters # | \ <escape>
     * 
     * <escape>     ::= \" | \ | / | b | f | n | r | t | u <hex><hex><hex><hex>
     * 
     * <number>     ::= <int> <frac>? <exp>?
     * <int>        ::= -? <digits>
     * <frac>       ::= . <digits>
     * <exp?        ::= (e | E) (+ | -)? <digits>
     * <digits>     ::= <digit> | <digit> <digits>
     * <digit>      ::= # digit from 0 to 9 #
     * <hex>        ::= <digit> | [a-f] | [A-F]
     */

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
    
    /** Tracks the unique keys in the JSON tree structure. */
    private static TreeSet<Set<String>> keys;

    public static boolean validateJson(String jsonLine) {
        return true;
    }

    public static boolean validateObject(String objectLine) {
        return true;
    }

    public static boolean validateMembers(String membersLine) {
        return true;
    }

    public static boolean validatePair(String pairLine) {
        return true;
    }

    public static boolean validateArray(String arrayLine) {
        return true;
    }

    public static boolean validateElements(String elementsLine) {
        return true;
    }

    public static boolean validateValue(String valueLine) {
        return true;
    }

    public static boolean validateString(String stringLine) {
        return true;
    }

    public static boolean validateCharacters(String charactersLine) {
        return true;
    }

    public static boolean validateCharacter(String characterLine) {
        return true;
    }

    public static boolean validateEscape(String escapeLine) {
        return true;
    }

    public static boolean validateNumber(String numberLine) {
        return true;
    }

    public static boolean validateInt(String intLine) {
        return true;
    }

    public static boolean validateFrac(String fracLine) {
        return true;
    }

    public static boolean validateExp(String expLine) {
        return true;
    }

    public static boolean validateDigits(String digitsLine) {
        return true;
    }

    public static boolean validateDigit(String digitLine) {
        return true;
    }

    public static boolean validateHex(String hexLine) {
        return true;
    }
}
