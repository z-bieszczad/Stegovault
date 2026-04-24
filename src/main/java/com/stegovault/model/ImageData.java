package com.stegovault.model;


import java.awt.image.BufferedImage;
import java.nio.file.Path;


/**
 * Represents a carrier image
 * used in steganography.
 */
public record ImageData( BufferedImage image, Path path, int width, int height, int capacityBits) {
    public static ImageData from(BufferedImage image, Path path){
        int width=image.getWidth();
        int heigth=image.getHeight();

        // na razie tylko dla R
        int capacityBits= width*heigth;

        return new ImageData(image, path, width, heigth, capacityBits);
    }
}
