package com.sjq.live.service;

import com.sjq.live.constant.LiveConfiguration;
import com.sjq.live.constant.PublishEnum;
import com.sjq.live.constant.SubscribeEnum;
import com.sjq.live.model.LiveException;
import com.sjq.live.model.OperateResponse;
import com.sjq.live.support.*;
import com.sjq.live.utils.media.StreamConverter;
import com.sjq.live.utils.media.StreamConverterHandler;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class DefaultTransformStreamHandler implements TransformStreamHandler {

    private static final Logger logger = LoggerFactory.getLogger(DefaultTransformStreamHandler.class);

    @Autowired
    private LiveConfiguration liveConfiguration;
    @Autowired
    private StreamConverter streamConverter;

    private static final Map<String, OutputStreamProcessor> OUT_STREAM_MAP = new ConcurrentHashMap<>();
    private static final Map<String, DistributeStreamProcessor> DISTRIBUTE_STREAM_MAP = new ConcurrentHashMap<>();
    private static final Map<String, Set<String>> SUBSCRIBE_USER_IDS_MAP = new ConcurrentHashMap<>();

    @Override
    public OperateResponse<PublishHandler> publish(String publishId) throws LiveException {
        //同一个流，只允许发布一次
        if (OUT_STREAM_MAP.containsKey(publishId)) {
            return new OperateResponse<>(PublishEnum.DUPLICATE_PUBLISH);
        }

        //开启视频流转换
        final StreamConverterHandler streamConverterHandler = streamConverter.startConvert(liveConfiguration.buildOriginStreamUrl(publishId),
                liveConfiguration.buildTransformedStreamUrl(publishId));

        //寻找输出流处理器
        OutputStreamProcessor out;
        do {
            out = OUT_STREAM_MAP.get(publishId);
        } while (!Objects.nonNull(out));

        //返回并携带句柄
        return new OperateResponse<>(PublishEnum.SUCCESS, new PublishHandler(out, () -> {
            streamConverterHandler.destory();
            OUT_STREAM_MAP.remove(publishId);
            DISTRIBUTE_STREAM_MAP.remove(publishId);
        }));
    }

    @Override
    public OperateResponse<SubscribeHandler> subscribe(final String userId,
                                                       final String subscribeId,
                                                       final AbstractStreamDistributeHandler handler) {
        if ((StringUtils.isEmpty(userId) || StringUtils.isEmpty(subscribeId) || Objects.isNull(handler))) {
            throw new IllegalArgumentException("参数不能为空");
        }

        Set<String> userIdSet = SUBSCRIBE_USER_IDS_MAP.computeIfAbsent(subscribeId, v -> ConcurrentHashMap.newKeySet());
        if (userIdSet.contains(userId)) {
            return new OperateResponse<>(SubscribeEnum.SUBSCRIBED);
        }
        //记录订阅记录
        userIdSet.add(userId);

        //添加handler
        if (!addSubscribeHandler(subscribeId, handler)) {
            return new OperateResponse<>(SubscribeEnum.NO_PUBLISHER);
        }

        //设置销毁回调方法
        handler.setDestroyCallBack(id -> {
            DistributeStreamProcessor task = DISTRIBUTE_STREAM_MAP.get(subscribeId);
            if (Objects.nonNull(task)) {
                task.removeSubscribeById(id);
            }
        });

        return new OperateResponse<>(SubscribeEnum.SUCCESS, new SubscribeHandler(handler));
    }

    private boolean addSubscribeHandler(final String subscribeId,
                                        final AbstractStreamDistributeHandler handler) {
        DistributeStreamProcessor task = DISTRIBUTE_STREAM_MAP.get(subscribeId);
        if (Objects.isNull(task)) {
            return false;
        }
        task.addSubscribe(handler);
        return true;
    }

    @Override
    public void processOriginalStream(final String publishId,
                                      final OutputStreamProcessor streamProcessor) {
        //注册管道流
        OUT_STREAM_MAP.put(publishId, streamProcessor);
        //开始从管道中读取数据
        streamProcessor.processData();
        logger.info("[processOriginalStream] publishId:{}, originStream流关闭", publishId);
    }

    @Override
    public void processTransformedStream(final String publishId,
                                         final InputStreamProcessor streamProcessor) {
        //开启读取transformedStream流的线程
        DistributeStreamProcessor processor = DISTRIBUTE_STREAM_MAP.get(publishId);
        if (Objects.isNull(processor)) {
            processor = new DistributeStreamProcessor(streamProcessor);
            //注册读流处理器
            DISTRIBUTE_STREAM_MAP.put(publishId, processor);
            //开始分发视频流数据
            processor.distribute();

            logger.info("[processTransformedStream] publishId:{}, transformedStream流关闭", publishId);
        } else {
            logger.info("[processTransformedStream] DistributeStreamProcessor[publishId:{}]已存在", publishId);
        }
    }
}
