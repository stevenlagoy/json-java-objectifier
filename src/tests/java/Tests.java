// package src.tests.java;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import core.FileOperations;
import core.JSONObject;
import core.Objectifier;

public class Tests {
    
    @Test
    public void objectifyNumbers() {
        Path dataPath = Path.of("src\\tests\\java\\data\\test1\\test1.json");
        Path expectedPath = Path.of("src\\tests\\java\\data\\test1\\expected1.out");
        String expected = String.join("\n", FileOperations.readFile(expectedPath));
        String actual = Objectifier.objectify(dataPath).toString();
        assertEquals(expected, actual);
    }

    @Test
    public void objectifySingleValues() {
        Path dataPath = Path.of("src\\tests\\java\\data\\test2\\test2.json");
        Path expectedPath = Path.of("src\\tests\\java\\data\\test2\\expected2.out");
        String expected = String.join("\n", FileOperations.readFile(expectedPath));
        String actual = Objectifier.objectify(dataPath).toString();
        assertEquals(expected, actual);
    }

    @Test
    public void objectifyStrings() {
        Path dataPath = Path.of("src\\tests\\java\\data\\test3\\test3.json");
        Path expectedPath = Path.of("src\\tests\\java\\data\\test3\\expected3.out");
        String expected = String.join("\n", FileOperations.readFile(expectedPath));
        String actual = Objectifier.objectify(dataPath).toString();
        assertEquals(expected, actual);
    }

    @Test
    public void objectifyNestedArrays() {
        Path dataPath = Path.of("src\\tests\\java\\data\\test4\\test4.json");
        Path expectedPath = Path.of("src\\tests\\java\\data\\test4\\expected4.out");
        String expected = String.join("\n", FileOperations.readFile(expectedPath));
        String actual = Objectifier.objectify(dataPath).toString();
        assertEquals(expected, actual);
    }

    @Test
    public void objectifyNestedObjects() {
        Path dataPath = Path.of("src\\tests\\java\\data\\test5\\test5.json");
        Path expectedPath = Path.of("src\\tests\\java\\data\\test5\\expected5.out");
        String expected = String.join("\n", FileOperations.readFile(expectedPath));
        String actual = Objectifier.objectify(dataPath).toString();
        assertEquals(expected, actual);
    }

    @Test
    public void testJSONObjectGetters() {
        JSONObject obj = new JSONObject("test", "value");
        assertEquals("test", obj.getKey());
        assertEquals("value", obj.getValue());
        assertEquals(String.class, obj.getType());
        assertEquals("value", obj.getAsString());
        assertNull(obj.getAsNumber());
        assertNull(obj.getAsBoolean());
        assertNull(obj.getAsObject());
        assertNull(obj.getAsList());
    }

    @Test
    public void testNestedObjectGet() {
        JSONObject nested = new JSONObject("inner", "value");
        JSONObject obj = new JSONObject("outer", nested);
        assertEquals("value", obj.get("inner"));
        assertNull(obj.get("nonexistent"));
    }

    @Test
    public void testListContentType() {
        List<Object> listWithObjects = new ArrayList<>();
        listWithObjects.add(new JSONObject("item1", "value1"));
        listWithObjects.add(new JSONObject("item2", "value2"));
        
        JSONObject obj = new JSONObject("test", listWithObjects);
        assertEquals(JSONObject.class, obj.getType());
    }

    @Test
    public void testEmptyListTypes() {
        List<Object> emptyObjectList = new ArrayList<>();
        JSONObject objList = new JSONObject("objects", emptyObjectList);
        objList.setValue(emptyObjectList, JSONObject.class);
        
        String result = objList.toString();
        assertTrue(result.contains("{}"));
        assertFalse(result.contains("[]"));
    }

    @Test
    public void testSingleLineArrayFormatting() {
        List<Object> shortList = Arrays.asList(1, 2, 3);
        JSONObject obj = new JSONObject("short", shortList);
        String result = obj.toString();
        assertTrue(result.contains("[1, 2, 3]"));
        assertFalse(result.contains("\n"));
    }

    @Test
    public void testMultiLineArrayFormatting() {
        List<Object> longList = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        JSONObject obj = new JSONObject("long", longList);
        String result = obj.toString();
        assertTrue(result.contains("\n"));
        assertTrue(result.split("\n").length > 2);
    }

    @Test
    public void testJSONValidityChecker() {
        Path[] dataPaths = {
            Path.of("src\\tests\\java\\data\\test1\\test1.json"),
            Path.of("src\\tests\\java\\data\\test2\\test2.json"),
            Path.of("src\\tests\\java\\data\\test3\\test3.json"),
            Path.of("src\\tests\\java\\data\\test4\\test4.json"),
            Path.of("src\\tests\\java\\data\\test5\\test5.json"),
            Path.of("src\\tests\\java\\data\\test6\\invalid0.json"),
            Path.of("src\\tests\\java\\data\\test6\\invalid1.json"),
            Path.of("src\\tests\\java\\data\\test6\\invalid2.json"),
            Path.of("src\\tests\\java\\data\\test6\\invalid3.json")
        };
        assertTrue(Objectifier.verifyJSONFile(dataPaths[0]));
        assertTrue(Objectifier.verifyJSONFile(dataPaths[1]));
        assertTrue(Objectifier.verifyJSONFile(dataPaths[2]));
        assertTrue(Objectifier.verifyJSONFile(dataPaths[3]));
        assertTrue(Objectifier.verifyJSONFile(dataPaths[4]));
        assertFalse(Objectifier.verifyJSONFile(dataPaths[5]));
        assertFalse(Objectifier.verifyJSONFile(dataPaths[6]));
        assertFalse(Objectifier.verifyJSONFile(dataPaths[7]));
        assertFalse(Objectifier.verifyJSONFile(dataPaths[8]));
    }

    @Test
    public void testJSONValidationWithInvalidKeys() {
        List<String> invalidKey = Arrays.asList(
            "{",
            "    key: \"value\"",  // key not quoted
            "}"
        );
        assertFalse(Objectifier.verifyJSONFile(invalidKey));
    }

    @Test
    public void testJSONValidationWithMissingCommas() {
        List<String> missingComma = Arrays.asList(
            "{",
            "    \"key1\": \"value1\"",  // missing comma
            "    \"key2\": \"value2\"",
            "}"
        );
        assertFalse(Objectifier.verifyJSONFile(missingComma));
    }

    @Test
    public void testJSONValidationWithExtraCommas() {
        List<String> extraComma = Arrays.asList(
            "{",
            "    \"key1\": \"value1\",",
            "    \"key2\": \"value2\",",  // extra comma
            "}"
        );
        assertFalse(Objectifier.verifyJSONFile(extraComma));
    }

    @Test
    public void testJSONValidationWithValidFormat() {
        List<String> valid = Arrays.asList(
            "{                              ",
            "    \"key1\": \"value1\",      ",
            "    \"key2\": {                ",
            "        \"nested\": \"value\"  ",
            "    },                         ",
            "    \"key3\": \"value3\"       ",
            "}                              "
        );
        assertTrue(Objectifier.verifyJSONFile(valid));
    }
}
