package com.mountains.bledemo.util;

public class HexUtil {


    /**
     * byte[] 转16进制字符串
     */
    public static String bytes2HexString(byte[] b) {
        StringBuilder sb = new StringBuilder();
        for(int i=0;i<b.length;i++) {
            sb.append(String.format("%02x", b[i]));
        }
        return sb.toString();
    }


    /**
     * byte[]转int
     */
    public static int bytesToInt(byte[] bytes) {
        int value = 0;
        // 由高位到低位
        for (int i = 0; i < bytes.length; i++) {
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
            result[i-start] = bytes[start];
        }
        return result;
    }
}
