package com.stegovault.model;


import java.awt.image.BufferedImage;
import java.nio.file.Path;

// zrobione na przyszlosc do uzycia w stegoservice i do zamiany za bufferedimage

/**
 * Represents a carrier image used in steganography.
 * <p>
 * Stores image metadata and calculated LSB capacity.
 * Used to determine how much data can be embedded.
 *
 * @param image the BufferedImage object
 * @param path file path of the image
 * @param width image width in pixels
 * @param height image height in pixels
 * @param capacityBits number of available bits for LSB embedding
 */
public record ImageData( BufferedImage image, Path path, int width, int height, int capacityBits) {
    /**
     * Creates ImageData from BufferedImage and file path.
     *
     * @param image source image
     * @param path image file path
     * @return ImageData with calculated width, height and capacity
     */
    public static ImageData from(BufferedImage image, Path path){
        int width=image.getWidth();
        int heigth=image.getHeight();

        // na razie tylko dla R
        int capacityBits= width*heigth;

        return new ImageData(image, path, width, heigth, capacityBits);
    }
}
