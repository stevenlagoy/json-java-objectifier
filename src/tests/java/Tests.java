import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.nio.file.Path;
import java.util.List;

import org.junit.Test;

import core.FileOperations;
import core.JSONProcessor;

public class Tests {

    @Test
    public void testNumbers() {
        String expected = String.join("\n", FileOperations.readFile(Path.of("src\\tests\\java\\data\\test1\\expected1.out")));
        assertNotNull("Expected file content should not be null", expected);
        
        String result = JSONProcessor.processJson(Path.of("src\\tests\\java\\data\\test1\\test1.json")).toString();
        assertNotNull("Processed JSON should not be null", result);
        
        assertEquals(expected, result);
    }

    @Test
    public void testSingleValues() {
        assertEquals(
            FileOperations.readFile(Path.of("src\\tests\\java\\data\\test1\\expected2.out")),
            JSONProcessor.processJson(Path.of("src\\tests\\java\\data\\test1\\test2.json"))
        );
    }

    @Test
    public void testStrings() {
        assertEquals(
            FileOperations.readFile(Path.of("src\\tests\\java\\data\\test1\\expected3.out")),
            JSONProcessor.processJson(Path.of("src\\tests\\java\\data\\test1\\test3.json"))
        );
    }

    @Test
    public void testArrays() {
        assertEquals(
            FileOperations.readFile(Path.of("src\\tests\\java\\data\\test1\\expected4.out")),
            JSONProcessor.processJson(Path.of("src\\tests\\java\\data\\test1\\test4.json"))
        );
    }

    @Test
    public void testObjects() {
        assertEquals(
            FileOperations.readFile(Path.of("src\\tests\\java\\data\\test1\\expected5.out")),
            JSONProcessor.processJson(Path.of("src\\tests\\java\\data\\test1\\test5.json"))
        );
    }
    
}
