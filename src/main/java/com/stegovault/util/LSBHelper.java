package com.stegovault.util;


/**
 *class for Least Significant Bit (LSB) operations on integer values.
 *
 * <p>This class provides helper methods for reading and modifying individual bits,
 * which can be used in steganography algorithms to embed or extract hidden data.</p>
 */
public class LSBHelper {
    /**
     * Sets a bit at the specified position in the given integer value.
     *
     * @param value the original integer value
     * @param position the bit position to modify (0 = least significant bit)
     * @param bit the bit value to set (0 or 1)
     * @return the modified integer with the updated bit at the given position
     */
    public static int setBit(int value, int position, int bit){
        if(bit==1){
            return value| (1<<position);
        }else {
            return value & ~(1<<position);
        }
    }

    /**
     * Returns the value of a bit at the specified position in the given integer.
     *
     * @param value the integer value to read from
     * @param position the bit position (0 = least significant bit)
     * @return 0 or 1 depending on the bit value at the given position
     */
    public static int getBit(int value, int position){
        return (value>>position)&1;
    }


    /**
     * Changes Byte[] payload to individual bits
     *
     * @param data payload that will be chnged
     * @return bits table transformed from payload
     */
    public static int[] bytesToBits(byte[] data){
        int[] bits=new int[data.length*8];
        for(int i=0; i<data.length; i++){
            for(int j=0; j<8; j++){
                bits[i*8+j]=(data[i]>>(7-j))&1;
            }
        }return bits;
    }
}
