package com.sjq.live.controller;

import com.google.common.collect.Sets;
import com.sjq.live.constant.SubscribeEnum;
import com.sjq.live.model.SubscribeResponse;
import com.sjq.live.support.StreamWriteHandler;
import com.sjq.live.support.*;
import com.sjq.live.utils.ffmepg.FFmpegException;
import com.sjq.live.utils.ffmepg.FfmpegUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

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

    private static final String ORIGIN_STREAM_URL = "https://%s:%s/originStream?publishId=%s";
    private static final String TRANSFORMED_STREAM_URL = "https://%s:%s/transformedStream?publishId=%s";

    private static final Map<String, OutputStreamProcessor> outStreamMap = new ConcurrentHashMap<>();
    private static final Map<String, DistributeStreamProcessor> distributeStreamMap = new ConcurrentHashMap<>();
    private static final Map<String, Set<String>> subscribeUserIdsMap = new ConcurrentHashMap<>();

    private String buildOriginStreamUrl(final String publishId) {
        return String.format(ORIGIN_STREAM_URL, serverIp, serverPort, publishId);
    }

    private String buildTransformedStreamUrl(final String publishId) {
        return String.format(TRANSFORMED_STREAM_URL, serverIp, serverPort, publishId);
    }

    public StreamWriteHandler publish(String publishId) throws FFmpegException {
        if (outStreamMap.containsKey(publishId)) {//同一个流，只允许发布一次
            return null;
        }

        //开启ffmpeg视频流转换进程，ffmpeg会调用originStream读取原始视频流，然后调用transformedStream输出转换后的视频流
        final Process process = FfmpegUtil.convertStream(buildOriginStreamUrl(publishId), buildTransformedStreamUrl(publishId));

        //寻找输出流处理器
        OutputStreamProcessor out;
        for (;;) {
            out = outStreamMap.get(publishId);;
            if (Objects.nonNull(out)) {
                break;
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
    public SubscribeResponse<String> subscribe(final String userId,
                                               final String subscribeId,
                                               final AbstractLiveStreamHandler handler) {
        if ((StringUtils.isEmpty(userId) || StringUtils.isEmpty(subscribeId) || Objects.isNull(handler))) {
            throw new IllegalArgumentException("参数不能为空");
        }

        Set<String> userIdSet = subscribeUserIdsMap.computeIfAbsent(subscribeId, v -> ConcurrentHashMap.newKeySet());
        if (userIdSet.contains(userId)) {
            return new SubscribeResponse<>(SubscribeEnum.SUBSCRIBED);
        }

        DistributeStreamProcessor task = distributeStreamMap.get(subscribeId);
        if (Objects.isNull(task)) {
            return new SubscribeResponse<>(SubscribeEnum.NO_PUBLISHER);
        }

        //记录订阅记录
        userIdSet.add(userId);
        //设置handler唯一id
        handler.setId(UUID.randomUUID().toString());
        //添加handler
        task.addSubscribe(handler);

        return new SubscribeResponse<String>(SubscribeEnum.SUCCESS).data(handler.getId());
    }

    /**
     * 取消订阅直播
     * @param subscribeId
     * @param id
     */
    public void unSubscribe(String subscribeId, String id) {
        if (StringUtils.isEmpty(subscribeId) || StringUtils.isEmpty(id)) {
            return;
        }
        DistributeStreamProcessor task = distributeStreamMap.get(subscribeId);
        if (Objects.nonNull(task)) {
            task.getSubscribes().removeIf(abstractLiveStreamHandler -> StringUtils.equals(abstractLiveStreamHandler.getId(), id));
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
        final OutputStreamProcessor streamProcessor = new ServletOutputStreamProcessor(response.getOutputStream());
        outStreamMap.put(publishId, streamProcessor);
        //开始从管道中读取数据
        streamProcessor.processData();

        logger.info("publishId:{}, originStream流关闭", publishId);
    }

    /**
     * 经ffmpeg转换后的流(供ffmpeg调用)
     * @param request
     * @throws Exception
     */
    @RequestMapping("/transformedStream")
    public void transformedStream(HttpServletRequest request, String publishId) throws Exception {
        //开启读取transformedStream流的线程
        DistributeStreamProcessor task = distributeStreamMap.get(publishId);
        if (Objects.isNull(task)) {
            task = new DistributeStreamProcessor(new ServletInputStreamProcessor(request.getInputStream()));
            //注册读流任务
            distributeStreamMap.put(publishId, task);
            //开始分发视频流数据
            task.distribute();

            logger.info("publishId:{}, transformedStream流关闭", publishId);
        } else {
            logger.info("DistributeStreamProcessor[DistributeStreamProcessor:{}]已存在", publishId);
        }
    }
}
