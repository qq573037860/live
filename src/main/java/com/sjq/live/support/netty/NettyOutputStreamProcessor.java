package com.sjq.live.support.netty;

import com.sjq.live.support.OutputStreamProcessor;
import io.netty.channel.ChannelFuture;
import org.springframework.util.Assert;

public class NettyOutputStreamProcessor extends OutputStreamProcessor {

    private final NettyOutputStream outputStream;

    public NettyOutputStreamProcessor(final NettyOutputStream outputStream) {
        Assert.notNull(outputStream, "NettyOutputStream不能为空");

        this.outputStream = outputStream;
    }

    @Override
    public void writeToStream(final byte[] bytes) {
        outputStream.write(bytes);
    }

    @Override
    public void flushStream() {
        outputStream.flush();
    }

    @Override
    protected void closeStream() {
        ChannelFuture future = outputStream.close();
        NettyChannelAttribute.setLastChannelFuture(outputStream.getCtx(), future);
    }
}
