package com.stegovault.util;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;


public class FileUtil {

    private FileUtil(){


    }

    public static byte[] read(Path path)throws IOException{
        return Files.readAllBytes(path);
    }

    public static void write(Path path, byte[] data)throws IOException{
        Files.write(path, data);
    }

    public static void writeText(String content, Path path)throws IOException{
        Files.writeString(path, content);
    }


}
