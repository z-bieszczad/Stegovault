package com.stegovault;

import com.stegovault.util.LSBHelper;

import java.awt.image.BufferedImage;
import java.util.Arrays;

public class LSBHelperTest {

    public static void main(String[] args) {

        BufferedImage img =
                new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);

        byte[] data = "A".getBytes();

        int[] bits = LSBHelper.bytesToBits(data);

        LSBHelper.encodeBitsToImage(img, bits);

        int rgb = img.getRGB(0, 0);
        System.out.println("Pixel[0,0] RGB: " + rgb);


        System.out.println("Bits length: " + bits.length);


        System.out.println("First bit: " + bits[0]);

        byte[] dataa = "A".getBytes();

        int[] bitss = LSBHelper.bytesToBits(data);

        LSBHelper.encodeBitsToImage(img, bits);

        int[] decodedBits = LSBHelper.decodeBitsFromImage(img, 8);

        System.out.println("Decoded bits: ");
        for (int b : decodedBits) {
            System.out.print(b);
        }






    }
}