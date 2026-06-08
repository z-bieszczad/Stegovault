package com.stegovault.service.impl;

import java.awt.image.BufferedImage;
import java.nio.charset.StandardCharsets;

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

        byte[] plainData=text.getBytes(StandardCharsets.UTF_8);
        int estimatedPayload = 4 + 16 + 16 + 32 + plainData.length + 16;

        if(!validationService.validateCapacity(image, estimatedPayload)){
            throw new IllegalArgumentException(" image to small ");
        }
       

        byte[] encrypted=cryptoService.encrypt(plainData, config);

        byte[] hash = hashService.generateHash(plainData);

        byte[] payload= PayloadHelper.buildPayload(encrypted, config.salt(), config.iv(), hash);

        int[] bits= LSBHelper.bytesToBits(payload);




        LSBHelper.encodeBitsToImage(image, bits);

        return image;
    }

    @Override
    public String decode(BufferedImage image, String password) throws CryptoException {


        int totalBits = image.getWidth() * image.getHeight() * 3;

        int[] bits = LSBHelper.decodeBitsFromImage(image, totalBits);


        byte[] payload = LSBHelper.bitsToBytes(bits);


        ParsedPayload data = PayloadHelper.parsePayload(payload);

        EncryptionConfig config = new EncryptionConfig(
                password,
                data.salt(),
                data.iv(),
                100000
        );


        byte[] decrypted = cryptoService.decrypt(data.encryptedData(), config);
        hashService.verifyHash(decrypted, hashService.generateHash(data.hash()));

        return new String(decrypted, StandardCharsets.UTF_8);
    }


}