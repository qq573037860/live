package com.sjq.live.support;

import com.sjq.live.utils.FlvStreamParserUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;

import java.util.concurrent.ConcurrentLinkedQueue;

public class DistributeStreamProcessor {

    private final InputStreamProcessor inputStreamProcessor;
    private final ConcurrentLinkedQueue<AbstractStreamDistributeHandler> subscribes = new ConcurrentLinkedQueue<>();
    private byte[] headerData;//头部数据: flvHeader + keyFrames

    public DistributeStreamProcessor(final InputStreamProcessor inputStreamProcessor) {
        Assert.notNull(inputStreamProcessor, "ServletInputStreamProcessor不能为空");

        this.inputStreamProcessor = inputStreamProcessor;
    }

    public void distribute() {
        //解析flv视频流数据
        FlvStreamParserUtil.parseStream(inputStreamProcessor, (tagData, flvHeaderData, isTagHeaderStart) -> {
            //发送tag数据
            subscribes.parallelStream().forEach(abstractStreamDistributeHandler -> {
                abstractStreamDistributeHandler.send(tagData, flvHeaderData, isTagHeaderStart);
            });
        }, headerData -> {
            //保存解析出来的头部数据
            this.headerData = headerData;
        });
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
