package core;

import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;

public class JSONStringifier {

    public static void main(String[] args) {
        JSONObject json = JSONProcessor.processJson(Path.of("src\\tests\\java\\data\\test1\\test1.json"));
        System.out.println(JSONStringifier.stringifyJson(json));
    }
    
    /*
     * JSON BNF Grammar
     * <json>       ::= <value>
     * <value>      ::= <string> | <number> | <object> | <array> | true | false | null
     *
     * <object>     ::= { } | { <members> }
     * <members>    ::= <pair> | <pair> , <members>
     * <pair>       ::= <string> : <value>
     *                  semantics rule: <string> must be unique within its level
     *
     * <array>      ::= [ ] | [ <elements> ]
     * <elements>   ::= <value> | <value> , <elements>
     *
     * <string>     ::= " <characters> "
     * <characters> ::= <character> | <character> <characters>
     * <character>  ::= # any unicode character except " or \ or control characters # | <escape>
     *
     * <escape>     ::= \ (" | \ | / | b | f | n | r | t | u <hex><hex><hex><hex>)
     *
     * <number>     ::= <int> <frac>? <exp>?
     * <int>        ::= -? <digits>
     * <frac>       ::= . <digits>
     * <exp>        ::= (e | E) (+ | -)? <digits>
     * <digits>     ::= <digit> | <digit> <digits>
     * <digit>      ::= # digit from 0 to 9 #
     * <hex>        ::= <digit> | [a-f] | [A-F]
     */

    public static String stringifyJson(JSONObject json) {
        return stringifyValue(json.getValue());
    }

    public static String stringifyValue(Object value) {
        if (value instanceof List) {
            List<?> list = (List<?>) value;
            if (!list.isEmpty()) {
                
            }
            return stringifyArray((List<?>) value);
        }
        if (value instanceof String) return "\"" + stringifyEscape((String) value) + "\"";
        if (value instanceof Number || value instanceof Boolean) return value.toString();
        if (value == null) return "null";
        else throw new IllegalArgumentException("Unsupported JSON value: " + value);
    }
    public static String stringifyValue(Object value, Class<?> type) {
        return null;
    }

    public static String stringifyObject(JSONObject object) {
        if (object == null) return "{}";
        
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        Iterator<?> it = object.iterator();
        while (it.hasNext()) {
            sb.append(stringifyValue(object.getKey())).append(" : ");
            sb.append(stringifyValue(it.next()));
            if (it.hasNext()) sb.append(", ");
        }
        sb.append("}");

        return sb.toString();
    }

    public static String stringifyArray(List<?> array) {
        if (array == null || array.isEmpty()) return "[]";

        StringBuilder sb = new StringBuilder();
        sb.append("[");
        Iterator<?> it = array.iterator();
        while (it.hasNext()) {
            sb.append(stringifyValue(it.next()));
            if (it.hasNext()) sb.append(", ");
        }
        sb.append("]");

        return sb.toString();
    }

    public static String stringifyEscape(String escape) {
        return escape
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\b", "\\b")
            .replace("\f", "\\f")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t");
    }

}
