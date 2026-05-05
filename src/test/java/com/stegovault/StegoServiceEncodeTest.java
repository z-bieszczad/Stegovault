package com.stegovault;

import com.stegovault.model.EncryptionConfig;
import com.stegovault.service.*;
import com.stegovault.service.impl.*;

import java.awt.image.BufferedImage;

public class StegoServiceEncodeTest {

    public static void main(String[] args) throws Exception {

        BufferedImage img =
                new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);

        int before = img.getRGB(45,5);

        EncryptionConfig config = new EncryptionConfig(
                "passw",
                new byte[16],
                new byte[16],
                65536
        );

        CryptoService crypto = new CryptoServiceImpl();
        ValidationService validation = new ValidationServiceImpl();
        StegoService stego = new StegoServiceImpl(crypto, validation);


        stego.encode("Ala ma kota", config, img);

        int after = img.getRGB(45,5);



        System.out.println(Integer.toBinaryString(before));
        System.out.println(Integer.toBinaryString(after));
        System.out.println("DIFF: " + (before ^ after));
    }
}