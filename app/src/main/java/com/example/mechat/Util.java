package com.example.mechat;

public class Util {

    /**
     * 把int类型数据转换为4byte数组
     *
     * @param w
     * @return
     */
    public static byte[] IntToByteArr(int w) {
        byte[] bytes = new byte[4];
        bytes[0] = (byte) (w >> (3 * 8));
        bytes[1] = (byte) ((w >> (2 * 8)) & 0xff);
        bytes[2] = (byte) ((w >> 8) & 0xff);
        bytes[3] = (byte) w;
        return bytes;
    }

    /**
     * 将byte数组转换为int
     *
     * @param bys
     * @return
     */
    public static int ByteArrToInt(byte[] bys) {
        int sum = 0;
        int end = 4;
        byte len = 4;
        for (int i = 0; i < end; i++) {
            int n = ((int) bys[i]) & 0xff;
            n <<= (--len) * 8;
            sum += n;
        }
        return sum;
    }

}