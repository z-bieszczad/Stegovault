package com.stegovault;
import com.stegovault.util.LSBHelper;


public class LSBHelperTest {
    public static void main(String[] args) {

        byte[] data = "A".getBytes(); // 65

        int[] bits = LSBHelper.bytesToBits(data);

        for (int bit : bits) {
            System.out.print(bit);
        }
    }
}
