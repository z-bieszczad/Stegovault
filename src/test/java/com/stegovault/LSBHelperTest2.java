
package com.stegovault;

import com.stegovault.util.LSBHelper;

import java.awt.image.BufferedImage;

public class LSBHelperTest2 {

    public static void main(String[] args) {

        BufferedImage img =
                new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);

        byte[] data = "A".getBytes();

        int[] bits = LSBHelper.bytesToBits(data);


        LSBHelper.encodeBitsToImage(img, bits);


        int[] decodedBits = LSBHelper.decodeBitsFromImage(img, 8);


        byte[] result = LSBHelper.bitsToBytes(decodedBits);


        System.out.println("Result: " + new String(result));
    }
}