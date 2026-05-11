package com.stegovault.service;
import java.awt.image.BufferedImage;

import com.stegovault.exception.CryptoException;
import com.stegovault.model.EncryptionConfig;


public interface StegoService 
{
    BufferedImage encode(String text, EncryptionConfig config, BufferedImage image) throws CryptoException;
    String decode(BufferedImage image, EncryptionConfig config) throws CryptoException;
}
