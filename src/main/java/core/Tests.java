package core;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Tests {
    
    public static void main(String[] args) {
        
        System.setProperty("file.encoding", "UTF-8");
        System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));

        try {
            // Create the JSONObjects
            Set<Path> paths = FileOperations.listFiles(FilePaths.DATA_SOURCE, FileOperations.FileExtension.JSON);
            List<JSONObject> JSONs = new ArrayList<>();
            for (Path path : paths) {
                JSONs.add(Objectifier.objectify(path));
            }

            JSONObject json = JSONs.get(0);

            // for (Object entry : json) {
            //     System.out.println(entry);
            // }

            // Write the JSONObjects to a file
            try (FileWriter fw = new FileWriter(new File("data/result.out"))) {
                fw.write(json.toString());
            }

            // Check the file against the input: should be the same
            List<String> expected = Objectifier.readJSONLines(Path.of("data/expected.out"));
            List<String> result = Objectifier.readJSONLines(Path.of("data/result.out"));

            if (!Objectifier.verifyJSONFile(expected)) {
                System.out.println("FAILED: The original JSON file was malformed and should not have been Objectified");
                System.exit(2);
            }

            if (!Objectifier.verifyJSONFile(result)) {
                System.out.println("FAILED: The produced JSON file was malformed");
                System.exit(3);
            }

            if (expected.size() != result.size()) {
                System.out.println("FAILED: Mismatched size between expected (" + expected.size() + ") and result (" + result.size() + ").");
                for (int i = 1; i < expected.size(); i++) {
                    try {
                        if (!expected.get(i).equals(result.get(i))) {
                            System.out.println("\tFirst discrepency on line: " + (i+1));
                            System.out.println("\tExpected : " + expected.get(i) + "\n\t    read : " + result.get(i));
                            break;
                        }
                    }
                    catch (NullPointerException e) {
                        System.out.println("\tUnable to compare lines. Check line " + expected.size());
                    } 
                }
                System.exit(4);
            }
            for (int i = 1; i < expected.size(); i++) {
                if (!expected.get(i).equals(result.get(i))) {
                    System.out.println("FAILED: Comparison failed on line " + (i+1));
                    System.exit(5);
                }
            }

            // Pass
            System.out.println("PASSED");
            System.exit(0);

            
        }
        catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

}
