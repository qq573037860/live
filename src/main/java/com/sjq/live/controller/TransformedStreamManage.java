package com.sjq.live.controller;

import com.sjq.live.constant.SubscribeEnum;
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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by shenjq on 2019/11/29
 * 对外提供的流接口
 */

@Controller
public class TransformedStreamManage {

    private static final Logger logger = LoggerFactory.getLogger(TransformedStreamManage.class);

    @Value("${server.port}")
    private int serverPort;
    @Value("${server.extranet}")
    private String serverIp;

    private Map<String, PipedOutputStream> outStreamMap = new ConcurrentHashMap<>();
    private Map<String, PipedInputStream> inStreamMap = new ConcurrentHashMap<>();

    private void removeOutStream(String publishId) {
        outStreamMap.remove(publishId);
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

        //开始读取
        new StreamReadHandler(inStreamMap.get(subscribeId), handler).start();
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
            removeOutStream(publishId);
        }

    }

    class StreamReadHandler{

        private PipedInputStream inPipe;
        private ReadHandler handler;

        StreamReadHandler(PipedInputStream inPipe, ReadHandler handler) {
            this.handler = handler;
            this.inPipe = inPipe;
        }

        public void start() {
            new Thread(() -> {
                byte[] bytes = new byte[1024];
                int len;
                try {
                    while ((len = inPipe.read(bytes)) != -1) {
                        handler.read(bytes);
                    }
                    inPipe.close();
                } catch (Exception e) {
                    logger.error("读取流失败：", e);
                }
            }).start();
        }

    }

    public interface ReadHandler {

        void read(byte[] bytes);

    }
}
