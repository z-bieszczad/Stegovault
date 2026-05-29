package com.stegovault.util;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;

public class ImageUtil {
    private ImageUtil() {
    }

    public static BufferedImage read(Path path)throws IOException{
        return ImageIO.read(path.toFile());
    }

    public static void writePNG(BufferedImage image, Path path)throws IOException{
        ImageIO.write(image, "png",path.toFile());
    }
}
