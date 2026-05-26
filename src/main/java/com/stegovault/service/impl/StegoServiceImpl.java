package com.stegovault.service.impl;

import java.awt.image.BufferedImage;

import com.stegovault.exception.CryptoException;
import com.stegovault.model.EncryptionConfig;
import com.stegovault.model.ParsedPayload;
import com.stegovault.service.CryptoService;
import com.stegovault.service.HashService;
import com.stegovault.service.StegoService;
import com.stegovault.service.ValidationService;
import com.stegovault.util.LSBHelper;
import com.stegovault.util.PayloadHelper;

public class StegoServiceImpl implements StegoService {

    private final CryptoService cryptoService;
    private final ValidationService validationService;
    private final HashService hashService;

    public StegoServiceImpl(CryptoService cryptoService, ValidationService validationService, HashService hashService) {
        this.cryptoService = cryptoService;
        this.validationService = validationService;
        this.hashService = hashService;
    }

    @Override
    public BufferedImage encode(String text, EncryptionConfig config, BufferedImage image) throws CryptoException{
        // zmiana na bajty
        // crypto service
        // buildPayload
        //bytestobits
        //validation
        // enncodebitstoimage
        byte[] plainData=text.getBytes();

        byte[] encrypted=cryptoService.encrypt(plainData, config);

        byte[] hash = hashService.generateHash(plainData);

        byte[] payload= PayloadHelper.buildPayload(encrypted, config.salt(), config.iv(), hash );

        int[] bits= LSBHelper.bytesToBits(payload);


        if(!validationService.validateCapacity(image, payload.length)){
            throw new IllegalArgumentException(" image to small ");
        }

        LSBHelper.encodeBitsToImage(image, bits);

        return image;
    }

    @Override
    public String decode(BufferedImage image, EncryptionConfig config) throws CryptoException{

        int totalBits=image.getWidth()*image.getHeight()*3;

        int[] bits=LSBHelper.decodeBitsFromImage(image, totalBits);

        byte[] payload= LSBHelper.bitsToBytes(bits);

        ParsedPayload data=PayloadHelper.parsePayload(payload);

        byte[] decrypted=cryptoService.decrypt(data.encryptedData(), config);

        // rzuca wyjatek gdy hash sie nie zgadza
        hashService.verifyHash(decrypted, data.hash());

        return new String(decrypted);
    }

}