import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.nio.file.Path;
import java.util.List;

import org.junit.Test;

import core.FileOperations;
import core.JSONObject;
import core.JSONProcessor;

public class Tests {

    @Test
    public void testNumbers() {
        // Normalize path separators for cross-platform compatibility
        Path expectedPath = Path.of("src", "tests", "java", "data", "test1", "expected1.out");
        Path testPath = Path.of("src", "tests", "java", "data", "test1", "test1.json");

        // Read and normalize the expected content
        List<String> expectedLines = FileOperations.readFile(expectedPath);
        assertNotNull("Expected file content should not be null", expectedLines);
        String expected = String.join(System.lineSeparator(), expectedLines).trim();

        // Process and normalize the actual result
        JSONObject processed = JSONProcessor.processJson(testPath);
        assertNotNull("Processed JSON should not be null", processed);
        String result = processed.toString().trim();

        // Compare normalized strings
        assertEquals("JSON content does not match expected output", expected, result);
    }

    @Test
    public void testSingleValues() {
        assertEquals(FileOperations.readFile(Path.of("src\\tests\\java\\data\\test1\\expected2.out")),
                JSONProcessor.processJson(Path.of("src\\tests\\java\\data\\test1\\test2.json")));
    }

    @Test
    public void testStrings() {
        assertEquals(FileOperations.readFile(Path.of("src\\tests\\java\\data\\test1\\expected3.out")),
                JSONProcessor.processJson(Path.of("src\\tests\\java\\data\\test1\\test3.json")));
    }

    @Test
    public void testArrays() {
        assertEquals(FileOperations.readFile(Path.of("src\\tests\\java\\data\\test1\\expected4.out")),
                JSONProcessor.processJson(Path.of("src\\tests\\java\\data\\test1\\test4.json")));
    }

    @Test
    public void testObjects() {
        assertEquals(FileOperations.readFile(Path.of("src\\tests\\java\\data\\test1\\expected5.out")),
                JSONProcessor.processJson(Path.of("src\\tests\\java\\data\\test1\\test5.json")));
    }

}
