package com.stegovault;

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
    }
}