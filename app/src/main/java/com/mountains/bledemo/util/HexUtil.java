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


    public static int subBytesToInt(byte[] buffer, int byteNum, int begin, int end) {
        return bytes2Int(subBytes(buffer, begin, end), byteNum);
    }


    /**
     * byte[]转int
     */
    public static int bytes2Int(byte[] bytes) {
        int value = 0;
        // 由高位到低位
        for (int i = 0; i < 4; i++) {
            int shift = (4 - 1 - i) * 8;
            value += (bytes[i] & 0x000000FF) << shift;// 往高位游
        }
        return value;
    }

    /**
     * byte[]转int
     */
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

    public static int get1Bit(int val, int bitNum) {
        return (val >> bitNum) & 1;
    }

}
