package com.sjq.live.support.netty;

import com.sjq.live.model.LiveException;
import com.sjq.live.support.InputStreamProcessor;
import com.sjq.live.utils.NettyUtils;
import com.sjq.live.utils.Queue;
import io.netty.channel.ChannelHandlerContext;

import java.io.IOException;
import java.util.Objects;

public class NettyInputStreamProcessor implements InputStreamProcessor {

    private final ChunkDataHandler chunkDataHandler;

    private final ChannelHandlerContext ctx;

    public NettyInputStreamProcessor(ChunkDataHandler chunkDataHandler,
                                     ChannelHandlerContext ctx) {
        this.chunkDataHandler = chunkDataHandler;
        this.ctx = ctx;
    }

    @Override
    public byte[] read() throws IOException {
        return chunkDataHandler.poll();
    }

    @Override
    public void close() throws IOException {
        NettyUtils.writeHttpOkResponse(ctx);
    }

    public static class ChunkDataHandler {

        private final Queue<byte[]> chunkQueue = new Queue<>(10000);

        private volatile LiveException exception;

        private volatile boolean isProduceEnd;

        public void exceptionOccurred(LiveException exception) {
            this.exception = exception;
        }

        public void reachEnd() {
            isProduceEnd = true;
        }

        public void offer(byte[] data) {
            chunkQueue.offer(data);
        }

        public byte[] poll() throws IOException {
            byte[] data;
            for (;;) {
                data = chunkQueue.poll(100L);
                if (Objects.nonNull(data) || isProduceEnd) {
                    break;
                }
                if (Objects.nonNull(exception)) {
                    throw new IOException("netty连接异常", exception);
                }
            }
            return data;
        }
    }
}
