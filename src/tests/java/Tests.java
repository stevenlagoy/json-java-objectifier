import static org.junit.Assert.assertEquals;

import java.nio.file.Path;
import java.util.List;

import org.junit.Test;

import core.FileOperations;
import core.JSONProcessor;

public class Tests {

    @Test
    public void testNumbers() {
        String expected = String.join("\n", FileOperations.readFile(Path.of("src\\tests\\java\\data\\test1\\expected1.out")));
        String actual = JSONProcessor.processJson(Path.of("src\\tests\\java\\data\\test1\\test1.json")).toString();
        assertEquals(expected, actual);
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
