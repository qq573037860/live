package com.sjq.live.controller;

import com.sjq.live.constant.SubscribeEnum;
import com.sjq.live.support.StreamWriteHandler;
import com.sjq.live.support.*;
import com.sjq.live.utils.ffmepg.FFmpegException;
import com.sjq.live.utils.ffmepg.FfmpegUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by shenjq on 2019/11/29
 * 直播流转换接口
 */

@Controller
public class TransformStreamManage {

    private static final Logger logger = LoggerFactory.getLogger(TransformStreamManage.class);

    @Value("${server.port}")
    private Integer serverPort;
    @Value("${server.extranet}")
    private String serverIp;

    private Map<String, OutputStreamProcessor> outStreamMap = new ConcurrentHashMap<>();
    private Map<String, DistributeStreamProcessor> distributeStreamMap = new ConcurrentHashMap<>();

    public StreamWriteHandler publish(String publishId) throws FFmpegException {
        /*if (!outStreamMap.containsKey(publishId)) {//同一个流，只允许发布一次
            return null;
        }*/
        //开启ffmpeg视频流转换进程，会在originStream读取原始数据，然后将转换后的数据发到transformedStream中
        final Process process = FfmpegUtil.convertStream("https://" + serverIp + ":" + serverPort + "/originStream?publishId=" + publishId,
                    "https://" + serverIp + ":" + serverPort + "/transformedStream?publishId=" + publishId);

        //寻找输出流处理器
        OutputStreamProcessor out = null;
        for (;;) {
            if (null == out) {
                out = outStreamMap.get(publishId);;
                if (null != out) {
                    break;
                }
            }
        }

        //返回写入流句柄
        return new StreamWriteHandler(out, () -> {
            process.destroyForcibly();
            outStreamMap.remove(publishId);
            distributeStreamMap.remove(publishId);
        });
    }

    /**
     * 订阅直播
     * @param subscribeId
     * @param handler
     * @return
     */
    public SubscribeEnum subscribe(String subscribeId, AbstractLiveStreamHandler handler) {
        if (StringUtils.isEmpty(subscribeId) || null == handler) {
            throw new IllegalArgumentException("参数不能为空");
        }

        DistributeStreamProcessor task = distributeStreamMap.get(subscribeId);
        if (null == task) {
            return SubscribeEnum.NO_PUBLISHER;
        }
        handler.setId(UUID.randomUUID().toString());
        task.addSubscribe(handler);

        return SubscribeEnum.SUCCESS.name(handler.getId());
    }

    /**
     * 取消订阅直播
     * @param subscribeId
     * @param id
     */
    public void unSubscribe(String subscribeId, String id) {
        DistributeStreamProcessor task = distributeStreamMap.get(subscribeId);
        if (null != task) {
            ConcurrentLinkedQueue<AbstractLiveStreamHandler> handlers = task.getSubscribes();
            Iterator<AbstractLiveStreamHandler> it = handlers.iterator();
            while (it.hasNext()) {
                if (it.next().getId().equals(id)) {
                    it.remove();
                    return;
                }
            }
        }
    }

    /**
     * 原始流(供ffmpeg调用)
     * @param response
     * @throws Exception
     */
    @RequestMapping("/originStream")
    public void originStream(HttpServletResponse response, String publishId) throws Exception {
        //注册管道流
        OutputStreamProcessor streamProcessor = new ServletOutputStreamProcessor(response.getOutputStream());
        outStreamMap.put(publishId, streamProcessor);
        //从管道中读取数据
        streamProcessor.processingData();
        logger.info("publishId:{}, originStream流关闭", publishId);
    }

    /**
     * 经ffmpeg转换后的流(供ffmpeg调用)
     * @param request
     * @throws Exception
     */
    @RequestMapping("/transformedStream")
    public void transformedStream(HttpServletRequest request, String publishId) throws Exception {
        ServletInputStream in = request.getInputStream();

        //开启读取transformedStream流的线程
        DistributeStreamProcessor task = distributeStreamMap.get(publishId);
        if (null == task) {
            task = new DistributeStreamProcessor(new ServletInputStreamProcessor(in));
            //注册读流任务
            distributeStreamMap.put(publishId, task);
            task.distribute();
        }

        logger.info("publishId:{}, transformedStream流关闭", publishId);
    }
}
