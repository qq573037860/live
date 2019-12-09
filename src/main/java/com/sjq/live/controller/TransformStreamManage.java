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
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.LockSupport;

/**
 * Created by shenjq on 2019/11/29
 * 对外提供的流接口
 */

@Controller
public class TransformStreamManage {

    private static final Logger logger = LoggerFactory.getLogger(TransformStreamManage.class);

    @Value("${server.port}")
    private Integer serverPort;
    @Value("${server.extranet}")
    private String serverIp;

    private Map<String, Object[]> outStreamMap = new ConcurrentHashMap<>();
    private Map<String, StreamReadTask> taskMap = new ConcurrentHashMap<>();

    private void removeMap(String publishId) {
        outStreamMap.remove(publishId);
        taskMap.remove(publishId);
    }

    public StreamWriteHandler publish(String publishId) {
        /*if (!outStreamMap.containsKey(publishId)) {//同一个流，只允许发布一次
            return null;
        }*/
        //开启ffmpeg视频流转换进程，会在originStream读取原始数据，然后将转换后的数据发到transformedStream中
        Process process = null;
        try {
            process = FfmpegUtil.convertStream("http://" + serverIp + ":" + serverPort + "/originStream?publishId=" + publishId,
                    "http://" + serverIp + ":" + serverPort + "/transformedStream?publishId=" + publishId);
        } catch (FFmpegException e) {
            logger.error("开启ffmpeg视频流转换进程失败", e);
            return null;
        }
        //寻找写入管道流
        ServletOutputStream out = null;
        Thread handler = null;
        for (;;) {
            if (null == out || null == handler) {
                Object[] arr = outStreamMap.get(publishId);
                if (null == out) {
                    continue;
                }
                out = (ServletOutputStream) arr[0];
                handler = (Thread) arr[1];
                break;
            }
        }

        return new StreamWriteHandler(out, handler, process, publishId);
    }

    public SubscribeEnum subscribe(String subscribeId, ReadHandler handler) {
        if (null == handler) {
            return SubscribeEnum.READ_HANDLER_IS_NULL;
        }

        StreamReadTask task = taskMap.get(subscribeId);
        if (null == task) {
            return SubscribeEnum.NO_PUBLISHER;
        }
        handler.setId(UUID.randomUUID().toString());
        task.addSubscribe(handler);

        return SubscribeEnum.SUCCESS.name(handler.getId());
    }

    public void unSubscribe(String subscribeId, String id) {
        StreamReadTask task = taskMap.get(subscribeId);
        if (null != task) {
            List<ReadHandler> list = task.getSubscribes();
            for (int i = 0, length = list.size(); i < length; i++) {
                if (list.get(i).getId().equals(id)) {
                    list.remove(i);
                    break;
                }
            }
        }
    }

    /**
     * 原始流
     * @param response
     * @throws Exception
     */
    @RequestMapping("/originStream")
    public void originStream(HttpServletResponse response, String publishId) throws Exception {
        ServletOutputStream out = response.getOutputStream();

        Thread currentThread = Thread.currentThread();
        //注册管道流
        outStreamMap.put(publishId, new Object[]{out, currentThread});
        LockSupport.park(currentThread);

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

        //开启读取transformedStream流的线程
        StreamReadTask task = taskMap.get(publishId);
        if (null == task) {
            task = new StreamReadTask(in);
            //注册读流任务
            taskMap.put(publishId, task);
            task.run();
        }

        logger.info("publishId:{}, transformedStream流关闭", publishId);
    }

    public class StreamWriteHandler{

        private ServletOutputStream out;
        private Thread handler;
        private Process process;
        private String publishId;

        StreamWriteHandler(ServletOutputStream out, Thread handler, Process process, String publishId) {
            this.out = out;
            this.handler = handler;
            this.process = process;
            this.publishId = publishId;
        }

        public void write(byte[] bytes) {
            try {
                out.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        public void writeWithFlush(byte[] bytes) {
            write(bytes);
            try {
                out.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        public void close() {
            process.destroyForcibly();
            try {
                out.close();
                LockSupport.unpark(handler);
            } catch (IOException e) {
                e.printStackTrace();
            }
            removeMap(publishId);
        }

    }

    class StreamReadTask {

        private ServletInputStream in;
        private List<ReadHandler> subscribes = Collections.synchronizedList(new ArrayList<>());

        StreamReadTask(ServletInputStream inPipe) {
            this.in = inPipe;
        }

        public void addSubscribe(ReadHandler handler) {
            this.subscribes.add(handler);
        }

        List<ReadHandler> getSubscribes() {
            return subscribes;
        }

        public void run() {
            //头部数据，包含flvHeader 和 keyFrames
            ByteArrayOutputStream headerDataOutStream = new ByteArrayOutputStream();
            byte[] headerData = null;

            //读取flvHeader
            byte[] flvHeader = new byte[13];
            try {
                in.read(flvHeader);
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
                while ((len = in.read(bytes)) != -1) {
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
                                        headerDataOutStream = null;
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
                            boolean isTagHeaderStart = sendTagHeader;
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
                                    handler.read(tagData, headerData, isTagHeaderStart);
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
                in.close();
            } catch (Exception e) {
                logger.error("读取流失败：", e);
            }
        }
    }

    public static abstract class ReadHandler {

        private String id;
        private boolean isFirst = true;

        private void read(byte[] bytes, byte[] headerData, boolean isTagHeaderStart) {
            if (isFirst) {//第一次发送的起始数据(包含flvHeader 和 keyFrames)
                if (isTagHeaderStart) {//要从一个tagHeader开始读数据
                    read(headerData);
                    isFirst = false;
                }
                if (isFirst) {
                    return;
                }
            }
            read(bytes);
        }

        public abstract void read(byte[] bytes);

        private String getId() {
            return id;
        }

        private void setId(String id) {
            this.id = id;
        }
    }
}
