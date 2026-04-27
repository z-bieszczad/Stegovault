package com.stegovault.service.impl;

import com.stegovault.service.ValidationService;
import java.awt.image.BufferedImage;

/**
 * Service responsible for validating whether data can be embedded
 * into a carrier image using LSB steganography.
 */
public class ValidationServiceImpl implements ValidationService{
    @Override
    public boolean validateCapacity(BufferedImage image, int payloadSizeBytes){
        int avaliableBits=image.getWidth()* image.getHeight();// na razie jesen bit (dla R) #####       potem *3

        int requiredBits=payloadSizeBytes*8;

        return requiredBits<=avaliableBits;
    }
}
