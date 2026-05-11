package com.stegovault;

import com.stegovault.model.EncryptionConfig;
import com.stegovault.service.CryptoService;
import com.stegovault.service.StegoService;
import com.stegovault.service.ValidationService;
import com.stegovault.service.impl.CryptoServiceImpl;
import com.stegovault.service.impl.StegoServiceImpl;
import com.stegovault.service.impl.ValidationServiceImpl;

import java.awt.image.BufferedImage;

public class StegoFullTest {

    public static void main(String[] args) throws Exception {


        BufferedImage img = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);


        EncryptionConfig config = new EncryptionConfig(
                        "passw",
                        new byte[16],
                        new byte[16],
                        65536
                );


        CryptoService crypto = new CryptoServiceImpl();
        ValidationService validation = new ValidationServiceImpl();
        StegoService stego = new StegoServiceImpl(crypto, validation);


        String input = "Ala ma kota";


        stego.encode(input, config, img);


        String output = stego.decode(img, config);


        System.out.println("INPUT : " + input);
        System.out.println("OUTPUT: " + output);

        System.out.println("EQUAL: " + input.equals(output));
    }
}