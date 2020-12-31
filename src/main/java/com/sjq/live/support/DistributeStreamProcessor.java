package com.sjq.live.support;

import com.sjq.live.utils.FlvStreamParserUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;

public class DistributeStreamProcessor {

    private static final Logger logger = LoggerFactory.getLogger(DistributeStreamProcessor.class);

    private final InputStreamProcessor inputStreamProcessor;
    private final ConcurrentLinkedQueue<AbstractStreamDistributeHandler> subscribes = new ConcurrentLinkedQueue<>();
    private byte[] headerData;//头部数据: flvHeader + keyFrames

    public DistributeStreamProcessor(final InputStreamProcessor inputStreamProcessor) {
        Assert.notNull(inputStreamProcessor, "ServletInputStreamProcessor不能为空");

        this.inputStreamProcessor = inputStreamProcessor;
    }

    public void distribute() {
        //解析flv视频流数据
        try {
            FlvStreamParserUtil.parseStream(inputStreamProcessor, (tagData, flvHeaderData, isTagHeaderStart) -> {
                //发送tag数据
                subscribes.parallelStream().forEach(abstractStreamDistributeHandler -> {
                    abstractStreamDistributeHandler.send(tagData, flvHeaderData, isTagHeaderStart);
                });
            }, headerData -> {
                //保存解析出来的头部数据
                this.headerData = headerData;
            });
        } catch (IOException e) {
            logger.error("分发flv流失败", e);
        }
    }

    public void addSubscribe(final AbstractStreamDistributeHandler handler) {
        //先把头部数据发送出去
        handler.send(headerData);
        this.subscribes.add(handler);
    }

    public void removeSubscribeById(final String id) {
        subscribes.removeIf(handler -> StringUtils.equals(handler.getId(), id));
    }
}
