package com.sjq.live.support;

import com.sjq.live.utils.FlvStreamParserUtil;

import java.util.concurrent.ConcurrentLinkedQueue;

public class DistributeStreamProcessor {

    private ServletInputStreamProcessor inputStreamProcessor;
    private ConcurrentLinkedQueue<AbstractLiveStreamHandler> subscribes = new ConcurrentLinkedQueue();
    private byte[] headerData;//头部数据: flvHeader + keyFrames

    public DistributeStreamProcessor(ServletInputStreamProcessor inputStreamProcessor) {
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

    public void addSubscribe(AbstractLiveStreamHandler handler) {
        //先把头部数据发送出去
        handler.send(headerData);
        this.subscribes.add(handler);
    }

    public ConcurrentLinkedQueue<AbstractLiveStreamHandler> getSubscribes() {
        return subscribes;
    }
}
