package com.stegovault;

import com.stegovault.model.ImageData;

import java.awt.image.BufferedImage;



public class ImageDataTest {
    public static void  main(String[] args){
        BufferedImage img =
                new BufferedImage(
                        100,100,
                        BufferedImage.TYPE_INT_RGB
                );

        ImageData data =
                ImageData.from(img,null);

        System.out.println(data.width());
        System.out.println(data.capacityBits());
    }
}
