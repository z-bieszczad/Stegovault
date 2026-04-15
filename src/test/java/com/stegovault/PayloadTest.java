package com.stegovault;

import com.stegovault.model.ParsedPayload;
import com.stegovault.util.PayloadHelper;

import java.util.Arrays;

public class PayloadTest {

    public static void main(String[] args) {

        byte[] encryptedData = "HELLO".getBytes();
        byte[] salt = new byte[16];
        byte[] iv = new byte[16];
        byte[] hash = new byte[32];

        byte[] payload = PayloadHelper.buildPayload(encryptedData, salt, iv, hash);

        System.out.println("Payload size:"+ payload.length);

        System.out.println("Preview: "+Arrays.toString(Arrays.copyOf(payload, 12)));

        ParsedPayload parsed = PayloadHelper.parsePayload(payload);


        System.out.println("Salt OK: " + Arrays.equals(parsed.salt(), salt));
        System.out.println("IV OK: " + Arrays.equals(parsed.iv(), iv));
        System.out.println("Hash OK: " + Arrays.equals(parsed.hash(), hash));
        System.out.println("Data OK: " + Arrays.equals(parsed.encryptedData(), encryptedData));


        System.out.println("Payload size: " + payload.length);
    }
}