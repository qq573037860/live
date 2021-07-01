package com.sjq.live.support.netty;

import com.sjq.live.model.LiveException;
import com.sjq.live.model.NettyHttpContext;
import com.sjq.live.support.InputStreamProcessor;
import com.sjq.live.support.OutputStreamProcessor;
import com.sjq.live.utils.Queue;
import io.netty.channel.ChannelFuture;

import java.io.IOException;
import java.util.Objects;

public class NettyInputStreamProcessor implements InputStreamProcessor {

    private final ChunkDataHandler chunkDataHandler;

    private final NettyOutputStream outputStream;

    public NettyInputStreamProcessor(final NettyHttpContext request) {
        this.chunkDataHandler = request.getHttpRequest().getChunkDataHandler();
        this.outputStream = request.getOutputStream();
    }

    @Override
    public byte[] read() throws IOException {
        return chunkDataHandler.poll();
    }

    @Override
    public void close() throws IOException {
        ChannelFuture future = outputStream.close();
        NettyChannelAttribute.setLastChannelFuture(outputStream.getCtx(), future);
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
                data = chunkQueue.poll();
                if (Objects.nonNull(data) || isProduceEnd) {
                    if (isProduceEnd) {
                        System.out.println("延时：" + (System.currentTimeMillis() - OutputStreamProcessor.st));
                        System.out.println();
                    }
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
