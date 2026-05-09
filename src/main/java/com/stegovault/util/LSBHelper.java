package com.stegovault.util;
import java.awt.image.BufferedImage;

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

    // do odzyskiwania
    public static byte[] bitsToBytes(int[] bits){
        int byteCount= bits.length/8;
        byte[] result=new byte[byteCount];

        for(int i=0; i<byteCount; i++){
            int value=0;

            for(int j=0; j<8; j++){
                value=(value<<1)| bits[i*8+j];
            }
            result[i]=(byte)value;
        }
        return result;
    }

    // juz dla wszystkich kanałów
    /**
     * Sets the least significant bit (LSB) of a pixel's color channels. ##### NA RAZIE TYLKO CZERWONY KANAL !
     *
     * @param image the image with pixel to modify
     * @param x the x-coordinate of the pixel
     * @param y the y-coordinate of the pixel
     * @param bit the bit value to embed (0 or 1)
     */
    public static void setLSB(BufferedImage image, int x, int y, int channel, int bit){

        int rgb = image.getRGB(x, y);

        int alpha = (rgb >> 24) & 0xFF;
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;

        //r = setBit(r, 0, bit);
        switch (channel) {
            case 0-> r=setBit(r,0,bit);
            case 1-> g=setBit(g,0,bit);
            case 2-> b=setBit(b,0,bit);
        }

        int newRGB =
                (alpha << 24) |
                        (r << 16) |
                        (g << 8) |
                        b;

        image.setRGB(x, y, newRGB);
//

    }

    public static void encodeBitsToImage(BufferedImage image, int[] bits){
        int bitIndex=0;

        for(int y=0; y<image.getHeight(); y++){
            for(int x=0; x<image.getWidth(); x++){

                for(int channel=0; channel<3; channel++){

                    if(bitIndex >= bits.length){
                        return;
                    }

                    setLSB(image, x, y,channel, bits[bitIndex]);
                    bitIndex++;
                }


            }
        }
    }

//    if(bitIndex >= bits.length){
//        return bits;
//    }
//
//    int rgb= image.getRGB(x, y);
//    int r=(rgb>>16)& 0xFF;
//
//    bits[bitIndex]=getBit(r,0); // tu tez na razie tylko dla czerwonego
//    bitIndex++;


    public static int[] decodeBitsFromImage(BufferedImage image, int numberOfBits){
        int[] bits=new int[numberOfBits];
        int bitIndex=0;

        for(int y=0; y<image.getHeight(); y++){
            for(int x=0; x<image.getWidth(); x++){

                int rgb=image.getRGB(x,y);

                int r= (rgb>> 16)&1;
                int g= (rgb>> 8)& 1;
                int b= rgb& 1;

                int[] channels={r,g,b};

                for(int c=0; c<3; c++){

                    if(bitIndex >= numberOfBits){
                        return bits;
                    }

                    bits[bitIndex++] = channels[c];
                }
            }
        }
        return bits;
    }


}
