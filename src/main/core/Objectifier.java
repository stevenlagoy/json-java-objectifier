package src.main.core;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;

public class Objectifier {

    public static void main(String[] args) {
        
        try {
            Set<String> filenames = FileOperations.listFiles(FilePaths.DATA_SOURCE, FileOperations.FileExtension.JSON);
            for (String filename : filenames) {
                System.out.println(filename);
            }
        }
        catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        JSONObject<?> nameObject = new JSONObject<>("name", null);
        System.out.println(nameObject.getKey());
        System.out.println(nameObject.getValue());
        System.out.println(nameObject.getValue().getClass());
    }

    public static JSONObject<?> toJSONObject(Path path) {
        return null;
    } 

}
