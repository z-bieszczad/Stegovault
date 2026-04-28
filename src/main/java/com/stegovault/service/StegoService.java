package com.stegovault.service;
import com.stegovault.model.EncryptionConfig;
import java.awt.image.BufferedImage;


public interface StegoService 
{
    BufferedImage encode(String text, EncryptionConfig config, BufferedImage image) throws Exception;
    String decode(BufferedImage image, EncryptionConfig config) throws Exception;
}
