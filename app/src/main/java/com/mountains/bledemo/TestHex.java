package com.mountains.bledemo;

public class TestHex {

    public static void main(String[] value){
        byte[] bytes = new byte[4];
        bytes[0] = 0;
        bytes[1] = 0;
        bytes[2] = 10;
        bytes[3] = 53;
        int i = bytesToInt(subBytes(bytes,0,3));
        System.out.println(i);
    }

    /**
     * byte[]转int
     */
    public static int bytesToInt(byte[] bytes) {
        int value = 0;
        // 由高位到低位
        for (int i = 0; i < 4; i++) {
            int shift = (4 - 1 - i) * 8;
            value += (bytes[i] & 0x000000FF) << shift;// 往高位游
        }
        return value;
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

