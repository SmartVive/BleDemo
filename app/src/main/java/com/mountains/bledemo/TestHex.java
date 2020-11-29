package com.mountains.bledemo;

import java.util.Arrays;

public class TestHex {

    public static void main(String[] value)  {
        byte[] bytes = new byte[4];
        bytes[0] = 0;
        bytes[1] = 0;
        bytes[2] = 10;
        bytes[3] = 53;
        int i = bytesToInt(subBytes(bytes,3,3));
        //System.out.println(i);


        int i1 = -59 & 65535;
        byte num = -59 ;
        int i3 = num & 255;
        int i2 = ((64 & 255) *256 + (-59 & 255))& 65535;
        byte[] bytes1 = byteToBitOfArray(num);
        System.out.println(i2 >> 14);
        System.out.println(i2 & 16383);

    }

    public static byte[] byteToBitOfArray(byte b) {
        byte[] array = new byte[16];
        for (int i = 15; i >= 0; i--) {
            array[i] = (byte)(b & 1);
            b = (byte) (b >> 1);
        }
        return array;
    }

    /**
     * byte[]转int
     */
    public static int bytesToInt(byte[] bytes) {
        int value = 0;
        // 由高位到低位
        for (int i = 0; i < bytes.length; i++) {
            int shift = (bytes.length - 1 - i) * 8;
            value += (bytes[i] & 0x000000FF) << shift;// 往高位游
        }
        return value;
    }

    private static int bytes2Int(byte[] buffer, int byteNum) {
        int intValue = 0;
        for (int i = 0; i < buffer.length; i++) {
            intValue = ((buffer[i] & 255) << (((byteNum - 1) - i) * 8)) + intValue;
        }
        return intValue;
    }



    /**
     * 截取 byte[]
     */
    public static byte[] subBytes(byte[] bytes,int start,int end){
        byte[] result = new byte[end - start + 1];
        for (int i = start;i<=end;i++){
            result[i-start] = bytes[i];
        }
        return result;
    }
}

