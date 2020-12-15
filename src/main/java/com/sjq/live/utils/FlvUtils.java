package com.sjq.live.utils;

/**
 * Created by shenjq on 2019/12/5
 */
public class FlvUtils {

    public static final int FLV_HEADER_SIZE = 13;
    public static final int TAG_HEADER_SIZE = 11;
    //保存前一个tag的大小
    private static final int PREVIOUS_TAG_SIZE = 4;

    /**
     * 获取tag的时间戳
     *
     * @param tagHeader
     * @return
     */
    public static int getTimeStamp(byte[] tagHeader) {
        return byteArrayToInt(new byte[]{tagHeader[4], tagHeader[5], tagHeader[6]});
    }

    /**
     * 获取tagData的长度
     *
     * @param tagHeader
     * @return
     */
    public static int getTagDataSize(byte[] tagHeader) {
        return byteArrayToInt(new byte[]{tagHeader[1], tagHeader[2], tagHeader[3]}) + PREVIOUS_TAG_SIZE;
    }

    public static int byteArrayToInt(byte[] b) {
        return (b[0] & 0xFF) << 16 |
                (b[1] & 0xFF) << 8 |
                (b[2] & 0xFF);
    }

    public static byte[] byteMerger(byte[] bt1, byte[] bt2) {
        byte[] bt3 = new byte[bt1.length + bt2.length];
        System.arraycopy(bt1, 0, bt3, 0, bt1.length);
        System.arraycopy(bt2, 0, bt3, bt1.length, bt2.length);
        return bt3;
    }

}
