package com.sjq.live.support;

import com.sjq.live.utils.FlvStreamParserUtil;
import com.sjq.live.utils.Queue;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedQueue;

public class DistributeStreamProcessor {

    private static final Logger logger = LoggerFactory.getLogger(DistributeStreamProcessor.class);

    private final InputStreamProcessor inputStreamProcessor;
    private static final ConcurrentLinkedQueue<AbstractStreamDistributeHandler> subscribes = new ConcurrentLinkedQueue<>();
    private byte[] headerData;//头部数据: flvHeader + keyFrames

    public DistributeStreamProcessor(final InputStreamProcessor inputStreamProcessor) {
        Assert.notNull(inputStreamProcessor, "ServletInputStreamProcessor不能为空");

        this.inputStreamProcessor = inputStreamProcessor;
    }

    public void distribute() {
        //解析flv视频流数据
        try {
            /*SendJob sendJob = new SendJob();
            sendJob.start();*/
            FlvStreamParserUtil.parseStream(inputStreamProcessor, (tagData, flvHeaderData, isTagHeaderStart) -> {
                //发送tag数据
                subscribes.parallelStream().forEach(abstractStreamDistributeHandler -> {
                    abstractStreamDistributeHandler.send(tagData, flvHeaderData, isTagHeaderStart);
                });
                //sendJob.add(new SendHolder(isTagHeaderStart, flvHeaderData, tagData));
            }, headerData -> {
                //保存解析出来的头部数据
                this.headerData = headerData;
            });
        } catch (IOException e) {
            logger.error("分发flv流失败", e);
        }
    }

    public void addSubscribe(final AbstractStreamDistributeHandler handler) {
        if (Objects.nonNull(headerData)) {
            //先把头部数据发送出去
            handler.send(headerData);
        }
        subscribes.add(handler);
    }

    public void removeSubscribeById(final String id) {
        subscribes.removeIf(handler -> StringUtils.equals(handler.getId(), id));
    }

    public static class SendJob {
        private final Queue<SendHolder> chunkQueue = new Queue<>(2);

        public void add(SendHolder holder) {
            chunkQueue.offer(holder);
        }

        public void start() {
            new Thread(() -> {
                for (;;) {
                    SendHolder holder = chunkQueue.poll(10L);
                    if (Objects.isNull(holder)) {
                        continue;
                    }
                    try {
                        for (AbstractStreamDistributeHandler subscribe : subscribes) {
                            subscribe.send(holder.getTagData(), holder.getFlvHeaderData(), holder.isTagHeaderStart());
                        }
                    } catch (Exception e) {
                        logger.error("异常", e);
                        break;
                    }
                }

            }).start();
        }
    }

    public static class SendHolder {
        boolean isTagHeaderStart;
        byte[] flvHeaderData;
        byte[] tagData;

        public SendHolder(boolean isTagHeaderStart, byte[] flvHeaderData, byte[] tagData) {
            this.isTagHeaderStart = isTagHeaderStart;
            this.flvHeaderData = flvHeaderData;
            this.tagData = tagData;
        }

        public boolean isTagHeaderStart() {
            return isTagHeaderStart;
        }

        public byte[] getFlvHeaderData() {
            return flvHeaderData;
        }

        public byte[] getTagData() {
            return tagData;
        }
    }
}
