package com.stegovault.service.impl;

import com.stegovault.model.EncryptionConfig;
import com.stegovault.model.ParsedPayload;
import com.stegovault.service.CryptoService;
import com.stegovault.service.StegoService;
import com.stegovault.service.ValidationService;
import com.stegovault.util.LSBHelper;
import com.stegovault.util.PayloadHelper;

import java.awt.image.BufferedImage;

public class StegoServiceImpl implements StegoService {

    private final CryptoService cryptoService;
    private final ValidationService validationService;

    public StegoServiceImpl(CryptoService cryptoService, ValidationService validationService) {
        this.cryptoService = cryptoService;
        this.validationService = validationService;
    }

    @Override
    public BufferedImage encode(String text, EncryptionConfig cfg, BufferedImage image) throws Exception{
        // zmiana na bajty
        // crypto service
        // buildPayload
        //bytestobits
        //validation
        // enncodebitstoimage

        return image;
    }

    @Override
    public String decode(BufferedImage image, EncryptionConfig cfg) throws Exception{
        return new String();
    }

}