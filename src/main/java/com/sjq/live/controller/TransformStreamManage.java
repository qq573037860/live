package com.sjq.live.controller;

import com.sjq.live.constant.SubscribeEnum;
import com.sjq.live.utils.FlvUtils;
import com.sjq.live.utils.ffmepg.FFmpegException;
import com.sjq.live.utils.ffmepg.FfmpegUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by shenjq on 2019/11/29
 * 对外提供的流接口
 */

@Controller
public class TransformStreamManage {

    private static final Logger logger = LoggerFactory.getLogger(TransformStreamManage.class);

    @Value("${server.port}")
    private int serverPort;
    @Value("${server.extranet}")
    private String serverIp;

    private Map<String, PipedOutputStream> outStreamMap = new ConcurrentHashMap<>();
    private Map<String, PipedInputStream> inStreamMap = new ConcurrentHashMap<>();

    private Map<String, StreamReadTask> taskMap = new ConcurrentHashMap<>();

    private void removeMap(String publishId) {
        outStreamMap.remove(publishId);
        taskMap.remove(publishId);
    }

    public StreamWriteHandler publish(String publishId) {
        if (outStreamMap.containsKey(publishId)) {//同一个流，只允许发布一次
            return null;
        }
        //开启ffmpeg视频流转换进程，会在in中读取originStream，然后将transformedStream写入到out中
        Process process = null;
        try {
            process = FfmpegUtil.convertStream("http://" + serverIp + ":" + serverPort + "/originStream?publishId" + publishId,
                    "http://" + serverIp + ":" + serverPort + "/transformedStream?publishId" + publishId);
        } catch (FFmpegException e) {
            logger.error("开启ffmpeg视频流转换进程失败", e);
            return null;
        }
        //寻找管道流
        PipedOutputStream out = null;
        for (;;) {
            if (null == out) {
                out = outStreamMap.get(publishId);
                break;
            }
        }

        return new StreamWriteHandler(out, process, publishId);
    }

    public SubscribeEnum subscribe(String subscribeId, ReadHandler handler) {
        if (!outStreamMap.containsKey(subscribeId)) {
            return SubscribeEnum.NO_PUBLISHER;
        }
        if (!inStreamMap.containsKey(subscribeId)) {
            return SubscribeEnum.SUBSCRIBED;
        }
        if (null == handler) {
            return SubscribeEnum.READ_HANDLER_IS_NULL;
        }

        StreamReadTask task = taskMap.computeIfAbsent(subscribeId, k -> new StreamReadTask(inStreamMap.get(subscribeId)));
        task.addSubscribe(handler);
        inStreamMap.remove(subscribeId);

        return SubscribeEnum.SUCCESS;
    }

    /**
     * 原始流
     * @param response
     * @throws Exception
     */
    @RequestMapping("/originStream")
    public void originStream(HttpServletResponse response, String publishId) throws Exception {
        ServletOutputStream out = response.getOutputStream();
        //建立管道
        PipedOutputStream outPipe = new PipedOutputStream();
        PipedInputStream inPipe = new PipedInputStream();
        outPipe.connect(inPipe);
        //注册管道
        outStreamMap.put(publishId, outPipe);
        //从管道中读取视频流数据
        byte[] bytes = new byte[1024];
        int len;
        while ((len = inPipe.read(bytes)) != -1) {
            out.write(bytes, 0, len);
            out.flush();
        }
        inPipe.close();

        logger.info("publishId:{}, originStream流关闭", publishId);
    }

    /**
     * 经ffmpeg转换后的流
     * @param request
     * @throws Exception
     */
    @RequestMapping("/transformedStream")
    public void transformedStream(HttpServletRequest request, String publishId) throws Exception {
        ServletInputStream in = request.getInputStream();
        //建立管道
        PipedOutputStream outPipe = new PipedOutputStream();
        PipedInputStream inPipe = new PipedInputStream();
        outPipe.connect(inPipe);
        //注册管道
        inStreamMap.put(publishId, inPipe);
        //向管道中写视频流数据
        byte[] bytes = new byte[1024];
        int len;
        while ((len = in.read(bytes)) != -1) {
            outPipe.write(bytes, 0, len);
            outPipe.flush();
        }
        outPipe.close();
        in.close();

        logger.info("publishId:{}, transformedStream流关闭", publishId);
    }

    public class StreamWriteHandler{

        private PipedOutputStream outPipe;
        private Process process;
        private String publishId;

        StreamWriteHandler(PipedOutputStream outPipe, Process process, String publishId) {
            this.outPipe = outPipe;
            this.process = process;
            this.publishId = publishId;
        }

        public void write(byte[] bytes) {
            try {
                outPipe.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        public void writeWithFlush(byte[] bytes) {
            write(bytes);
            try {
                outPipe.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        public void close() {
            process.destroyForcibly();
            try {
                outPipe.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            removeMap(publishId);
        }

    }

    class StreamReadTask implements Runnable {

        private PipedInputStream inPipe;
        private List<ReadHandler> subscribes = Collections.synchronizedList(new ArrayList<>());

        StreamReadTask(PipedInputStream inPipe) {
            this.inPipe = inPipe;
        }

        public void addSubscribe(ReadHandler handler) {
            this.subscribes.add(handler);
        }

        @Override
        public void run() {
            //头部数据，包含flvHeader 和 keyFrames
            ByteArrayOutputStream headerDataOutStream = new ByteArrayOutputStream();
            byte[] headerData = null;

            //读取flvHeader
            byte[] flvHeader = new byte[13];
            try {
                inPipe.read(flvHeader);
                headerDataOutStream.write(flvHeader);
            } catch (IOException e) {
                e.printStackTrace();
            }

            //读取关键帧数据标志位
            boolean startReadKeyFrameTag = true;

            //tagHeader数据
            byte[] tagHeader = new byte[11];
            int tagHeaderSize = 0;

            //剩下需要读取的tagData的长度
            int leftTagDataToRead = 0;

            boolean sendTagHeader = false;
            byte[] bytes = new byte[1024];
            int len;
            try {
                while ((len = inPipe.read(bytes)) != -1) {
                    //tagHeader读取起始位置
                    int tagHeaderIndex = 0;
                    //tagData读取起始位置
                    int tagDataIndex = 0;
                    for (;;) {
                        //读取tagHeader
                        int tagHeaderNeedToRead = 0;
                        if ((tagHeaderNeedToRead = tagHeader.length - tagHeaderSize) > 0) {//tagHeader未读完
                            int leftLength = len - tagHeaderIndex;
                            int tagHeaderReadLength = leftLength < tagHeaderNeedToRead ? leftLength : tagHeaderNeedToRead;
                            System.arraycopy(bytes, tagHeaderIndex, tagHeader, tagHeaderSize, tagHeaderReadLength);
                            tagHeaderSize+=tagHeaderReadLength;
                            if (tagHeader.length == tagHeaderSize) {//读完了
                                tagDataIndex = tagHeaderIndex + tagHeaderReadLength;
                                leftTagDataToRead = FlvUtils.getTagDataSize(tagHeader);
                                if (startReadKeyFrameTag) {
                                    int timeStamp = FlvUtils.getTimeStamp(tagHeader);
                                    if (timeStamp > 0) {
                                        startReadKeyFrameTag = false;
                                        headerData = headerDataOutStream.toByteArray();
                                        headerDataOutStream.close();
                                    }
                                    headerDataOutStream.write(tagHeader);
                                }
                                sendTagHeader = true;
                            }
                            if (leftLength <= tagHeaderNeedToRead) {
                                break;
                            }
                        }

                        //读取tagData
                        if (startReadKeyFrameTag || subscribes.size() > 0) {
                            int readLength = len - tagDataIndex > leftTagDataToRead ? leftTagDataToRead : len - tagDataIndex;
                            byte[] tagData = null;
                            if (sendTagHeader) {
                                tagData = FlvUtils.byteMerger(tagHeader, Arrays.copyOfRange(bytes, tagDataIndex, tagDataIndex + readLength));
                                sendTagHeader = false;
                            } else {
                                tagData = Arrays.copyOfRange(bytes, tagDataIndex, tagDataIndex + readLength);
                            }

                            //写关键帧数据
                            if (startReadKeyFrameTag) {
                                headerDataOutStream.write(tagData);
                            } else {
                                //给订阅者发送数据
                                for (ReadHandler handler : subscribes) {
                                    handler.read(tagData, headerData);
                                }
                            }
                        }
                        leftTagDataToRead -= len - tagDataIndex;
                        if (leftTagDataToRead < 1) { // 读完了
                            if (leftTagDataToRead < 0) { //读完data之后还有剩余的
                                //重置读取tagHeader的参数
                                tagHeaderIndex = len + leftTagDataToRead;
                                tagHeaderSize = 0;
                            } else {
                                break;
                            }
                        } else {
                            break;
                        }
                    }

                }
                inPipe.close();
            } catch (Exception e) {
                logger.error("读取流失败：", e);
            }
        }
    }

    public static abstract class ReadHandler {

        private boolean isFirst = true;

        private void read(byte[] bytes, byte[] headerData) {
            if (isFirst) {//第一次发送的起始数据(包含flvHeader 和 keyFrames)
                read(headerData);
                isFirst = true;
            }
            read(bytes);
        }

        public abstract void read(byte[] bytes);

    }
}
