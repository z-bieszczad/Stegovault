package com.stegovault.util;
import java.security.SecureRandom;

public class CryptoUtil {

    private static final SecureRandom RANDOM=new SecureRandom();

    private CryptoUtil(){

    }

    public static byte[] generateSalt(){
        byte[] salt=new byte[16];

        RANDOM.nextBytes(salt);

        return salt;
    }

    public static byte[] generateIV() {

        byte[] iv = new byte[16];

        RANDOM.nextBytes(iv);

        return iv;
    }
}
