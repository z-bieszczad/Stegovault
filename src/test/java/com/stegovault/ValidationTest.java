package com.stegovault;

import com.stegovault.service.ValidationService;
import com.stegovault.service.impl.ValidationServiceImpl;

import java.awt.image.BufferedImage;

public class ValidationTest {
    public static void main(String[] args) {

        BufferedImage img =
                new BufferedImage(
                        100,
                        100,
                        BufferedImage.TYPE_INT_RGB
                );

        ValidationService v =
                new ValidationServiceImpl();

        System.out.println(
                v.validateCapacity(img,100)
        );
    }
}
