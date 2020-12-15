package com.sjq.live.utils;

import com.sjq.live.support.InputStreamProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

/**
 * 解析flv数据流
 */
public class FlvStreamParserUtil {

    private static final Logger logger = LoggerFactory.getLogger(FlvStreamParserUtil.class);

    private static final int BUFFER_SIZE = 1024 * 1024;

    private static byte[] readHeader(InputStreamProcessor in) {
        byte[] flvHeader = new byte[FlvUtils.FLV_HEADER_SIZE];
        try {
            in.read(flvHeader);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return flvHeader;
    }

    public static void parseStream(InputStreamProcessor in, DataCallBack dataCallBack, HeaderDataCallBack headerDataCallBack) {
        Assert.notNull(in, "InputStreamProcessor不能为空");
        Assert.notNull(dataCallBack, "Function不能为空");

        //头部数据，包含flvHeader 和 keyFrames
        ByteArrayOutputStream headerDataOutStream = new ByteArrayOutputStream();
        byte[] headerData = null;

        //读取flvHeader
        try {
            headerDataOutStream.write(readHeader(in));
        } catch (IOException e) {
            e.printStackTrace();
        }

        //读取关键帧数据标志位
        boolean startReadKeyFrameTag = true;

        //tagHeader数据
        byte[] tagHeader = new byte[FlvUtils.TAG_HEADER_SIZE];
        int tagHeaderSize = 0;

        //剩下需要读取的tagData的长度
        int leftTagDataToRead = 0;

        //当前是否为tagHeader
        boolean isTagHeaderStart = false;

        byte[] bytes = new byte[BUFFER_SIZE];
        int len;
        try {
            while ((len = in.read(bytes)) != -1) {
                //tagHeader读取起始位置
                int tagHeaderIndex = 0;
                //tagData读取起始位置
                int tagDataIndex = 0;
                for (; ; ) {
                    //读取tagHeader
                    int tagHeaderNeedToRead;
                    if ((tagHeaderNeedToRead = tagHeader.length - tagHeaderSize) > 0) {//tagHeader未读完
                        int leftLength = len - tagHeaderIndex;
                        int tagHeaderReadLength = leftLength < tagHeaderNeedToRead ? leftLength : tagHeaderNeedToRead;
                        System.arraycopy(bytes, tagHeaderIndex, tagHeader, tagHeaderSize, tagHeaderReadLength);
                        tagHeaderSize += tagHeaderReadLength;
                        if (tagHeader.length == tagHeaderSize) {//读完了
                            tagDataIndex = tagHeaderIndex + tagHeaderReadLength;
                            leftTagDataToRead = FlvUtils.getTagDataSize(tagHeader);
                            if (startReadKeyFrameTag) {
                                int timeStamp = FlvUtils.getTimeStamp(tagHeader);
                                if (timeStamp > 0) {
                                    startReadKeyFrameTag = false;
                                    headerData = headerDataOutStream.toByteArray();
                                    headerDataCallBack.onData(headerData);
                                    headerDataOutStream.close();
                                    headerDataOutStream = null;
                                }
                            }
                            isTagHeaderStart = true;
                        }
                        if (leftLength <= tagHeaderNeedToRead) {
                            break;
                        }
                    }

                    //读取tagData
                    if (leftTagDataToRead > 0) {
                        int readLength = len - tagDataIndex > leftTagDataToRead ? leftTagDataToRead : len - tagDataIndex;
                        byte[] tagData;
                        boolean isTagHeaderStartTemp = isTagHeaderStart;
                        if (isTagHeaderStart) {
                            tagData = FlvUtils.byteMerger(tagHeader, Arrays.copyOfRange(bytes, tagDataIndex, tagDataIndex + readLength));
                            isTagHeaderStart = false;
                        } else {
                            tagData = Arrays.copyOfRange(bytes, tagDataIndex, tagDataIndex + readLength);
                        }

                        //写关键帧数据
                        if (startReadKeyFrameTag) {
                            headerDataOutStream.write(tagData);
                        } else {
                            //回调数据
                            dataCallBack.onData(tagData, headerData, isTagHeaderStartTemp);
                        }
                    }
                    leftTagDataToRead -= len - tagDataIndex;
                    if (leftTagDataToRead >= 0) {//继续读
                        break;
                    }
                    //读完一个tag后之后还有剩余，重置读取tagHeader的参数
                    tagHeaderIndex = len + leftTagDataToRead;
                    tagHeaderSize = 0;
                }
            }
        } catch (Exception e) {
            logger.error("读取流失败：", e);
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 数据回调
     */
    public interface DataCallBack {
        void onData(byte[] bytes, byte[] flvHeaderData, boolean isTagHeaderStart);
    }

    /**
     * flvHeader + keyFrames
     */
    public interface HeaderDataCallBack {
        void onData(byte[] bytes);
    }
}
