package com.sjq.live.support;

import com.sjq.live.utils.FlvStreamParserUtil;
import org.springframework.util.Assert;

import java.util.concurrent.ConcurrentLinkedQueue;

public class DistributeStreamProcessor {

    private final ServletInputStreamProcessor inputStreamProcessor;
    private final ConcurrentLinkedQueue<AbstractLiveStreamHandler> subscribes = new ConcurrentLinkedQueue<>();
    private byte[] headerData;//头部数据: flvHeader + keyFrames

    public DistributeStreamProcessor(final ServletInputStreamProcessor inputStreamProcessor) {
        Assert.notNull(inputStreamProcessor, "ServletInputStreamProcessor不能为空");

        this.inputStreamProcessor = inputStreamProcessor;
    }

    public void distribute() {
        //解析flv视频流数据
        FlvStreamParserUtil.parseStream(inputStreamProcessor, (tagData, flvHeaderData, isTagHeaderStart) -> {
            //发送tag数据
            subscribes.parallelStream().forEach(abstractLiveStreamHandler -> {
                abstractLiveStreamHandler.send(tagData, flvHeaderData, isTagHeaderStart);
            });
        }, datas -> {
            //保存解析出来的头部数据
            headerData = datas;
        });
    }

    public void addSubscribe(final AbstractLiveStreamHandler handler) {
        //先把头部数据发送出去
        handler.send(headerData);
        this.subscribes.add(handler);
    }

    public ConcurrentLinkedQueue<AbstractLiveStreamHandler> getSubscribes() {
        return subscribes;
    }
}
