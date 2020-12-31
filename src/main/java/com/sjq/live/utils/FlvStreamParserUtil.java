package com.sjq.live.utils;

import com.sjq.live.support.InputStreamProcessor;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;

/**
 * 解析flv数据流
 */
public class FlvStreamParserUtil {

    private static final Logger logger = LoggerFactory.getLogger(FlvStreamParserUtil.class);

    private static byte[] readData(int minLen, InputStreamProcessor in) throws IOException {
        int totalLen = 0;
        byte[] rs = null;
        do {
            byte[] data = in.read();
            if (Objects.isNull(data)) {
                break;
            }
            totalLen += data.length;
            //合并数据
            if (Objects.isNull(rs)) {
                rs = data;
            } else {
                //构造合并之后的数组，在这里使用Arrays.copy方法，属于合并数组的空间用null填充。
                byte[] mergeData = Arrays.copyOf(rs, rs.length + data.length);
                // 将合并数组的数据复制到之前构造好的空间里null填充的数据的位置。
                System.arraycopy(data, 0, mergeData, rs.length, data.length);
                rs = mergeData;
            }
        } while (totalLen < minLen);
        return rs;
    }



    public static void parseStream(InputStreamProcessor in, DataCallBack dataCallBack, HeaderDataCallBack headerDataCallBack) throws IOException {
        Assert.notNull(in, "InputStreamProcessor不能为空");
        Assert.notNull(dataCallBack, "Function不能为空");

        //头部数据，包含flvHeader 和 keyFrames
        ByteArrayOutputStream headerDataOutStream = new ByteArrayOutputStream();
        byte[] headerData = null;

        try {
            //读取flvHeader
            byte[] readData = readData(FlvUtils.FLV_HEADER_SIZE, in);;
            headerDataOutStream.write(readData, 0, FlvUtils.FLV_HEADER_SIZE);

            //读取关键帧数据标志位
            boolean startReadKeyFrameTag = true;

            //tagHeader数据
            byte[] tagHeader = new byte[FlvUtils.TAG_HEADER_SIZE];
            int tagHeaderSize = 0;

            //剩下需要读取的tagData的长度
            int leftTagDataToRead = 0;

            //当前是否为tagHeader
            boolean isTagHeaderStart = false;

            byte[] bytes;
            int len;
            //处理多余数据
            int leftLen = readData.length - FlvUtils.FLV_HEADER_SIZE;
            if (leftLen > 0) {
                bytes = new byte[leftLen];
                System.arraycopy(readData, FlvUtils.FLV_HEADER_SIZE, bytes, 0, leftLen);
            } else {
                bytes = in.read();
                if (Objects.isNull(bytes)) {
                    logger.error("解析flv流结束");
                    return;
                }
            }

            do {
                len = bytes.length;
                //tagHeader读取起始位置
                int tagHeaderIndex = 0;
                //tagData读取起始位置
                int tagDataIndex = 0;
                for (;;) {
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
            } while ((bytes = in.read()) != null);
            logger.error("解析flv流结束");
        } catch (IOException e) {
            logger.error("解析flv流失败", e);
            throw e;
        } finally {
            in.close();
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
