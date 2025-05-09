import static org.junit.Assert.*;
import org.junit.Test;
import core.JSONObject;
import core.JSONStringifier;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

public class StringifierTests {

    @Test
    public void stringifyJson() {
        ArrayList<JSONObject> inner = new ArrayList<>();
        inner.add(new JSONObject("key1", "value1"));
        inner.add(new JSONObject("key2", 42));
        JSONObject nested = new JSONObject("nested", inner);
        String result = JSONStringifier.stringifyJson(nested);
        assertEquals("\"nested\" : {\"key1\" : \"value1\", \"key2\" : 42}", result);
    }

    @Test
    public void stringifyValue() {
        // Test primitive values
        assertEquals("\"hello\"", JSONStringifier.stringifyValue("hello"));
        assertEquals("42", JSONStringifier.stringifyValue(42));
        assertEquals("true", JSONStringifier.stringifyValue(true));
        assertEquals("null", JSONStringifier.stringifyValue(null));
        
        // Test with type information
        List<Integer> numbers = new ArrayList<>();
        numbers.add(1);
        numbers.add(2);
        numbers.add(3);
        assertEquals("[1, 2, 3]", JSONStringifier.stringifyValue(numbers, ArrayList.class));
        
        // Test nested objects with type
        List<JSONObject> objects = new ArrayList<>();
        objects.add(new JSONObject("key1", "value1"));
        objects.add(new JSONObject("key2", "value2"));
        String result = JSONStringifier.stringifyValue(objects, JSONObject.class);
        assertEquals("{\"key1\" : \"value1\"}{\"key2\" : \"value2\"}", result);
    }

}