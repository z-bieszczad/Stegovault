package com.stegovault.service.impl;

import com.stegovault.model.EncryptionConfig;
import com.stegovault.model.ParsedPayload;
import com.stegovault.service.CryptoService;
import com.stegovault.service.HashService;
import com.stegovault.service.StegoService;
import com.stegovault.service.ValidationService;
import com.stegovault.util.LSBHelper;
import com.stegovault.util.PayloadHelper;

import java.awt.image.BufferedImage;

public class StegoServiceImpl implements StegoService {

    private final CryptoService cryptoService;
    private final ValidationService validationService;
    //private final HashService hashService;

    public StegoServiceImpl(CryptoService cryptoService, ValidationService validationService) {
        this.cryptoService = cryptoService;
        this.validationService = validationService;
    }

    @Override
    public BufferedImage encode(String text, EncryptionConfig config, BufferedImage image) throws Exception{
        // zmiana na bajty
        // crypto service
        // buildPayload
        //bytestobits
        //validation
        // enncodebitstoimage
        byte[] plainData=text.getBytes();

        byte[] encrypted=cryptoService.encrypt(plainData, config);


        byte[] hash = new byte[32]; // na razie na chwile !!!!!!!!!!!!!!!!!!!!!!!!!! ############ trzeba zrobić hashService !!!!!!!!!!!!!!!!

        byte[] payload= PayloadHelper.buildPayload(encrypted, config.salt(), config.iv(), hash );

        int[] bits= LSBHelper.bytesToBits(payload);

        if(!validationService.validateCapacity(image, payload.length)){
            throw new IllegalArgumentException(" image to small ");
        }

        LSBHelper.encodeBitsToImage(image, bits);

        return image;
    }

    @Override
    public String decode(BufferedImage image, EncryptionConfig cfg) throws Exception{
        return new String();
    }

}