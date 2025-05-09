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
        
        Path expectedPath = Path.of("src", "tests", "java", "data", "test1", "expected1.out");
        Path testPath = Path.of("src", "tests", "java", "data", "test1", "test1.json");

        String expected = String.join("\n", FileOperations.readFile(expectedPath));
        String actual = JSONProcessor.processJson(testPath).toString();

        assertEquals(expected, actual);
    }

    @Test
    public void testSingleValues() {

        Path expectedPath = Path.of("src\\tests\\java\\data\\test1\\expected2.out");
        Path testPath = Path.of("src\\tests\\java\\data\\test1\\test2.json");

        assertEquals(
            FileOperations.readFile(expectedPath),
            JSONProcessor.processJson(testPath)
        );
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
