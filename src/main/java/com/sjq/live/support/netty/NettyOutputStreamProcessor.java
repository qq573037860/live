package com.sjq.live.support.netty;

import com.sjq.live.support.OutputStreamProcessor;
import com.sjq.live.utils.NettyUtils;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.util.Assert;

import javax.servlet.ServletOutputStream;
import java.io.IOException;

public class NettyOutputStreamProcessor extends OutputStreamProcessor {

    private final ChannelHandlerContext ctx;

    public NettyOutputStreamProcessor(final ChannelHandlerContext ctx) {
        Assert.notNull(ctx, "ChannelHandlerContext不能为空");

        this.ctx = ctx;
    }

    @Override
    public void writeToStream(final byte[] bytes) throws IOException {
        NettyUtils.responseHttp(bytes, null, ctx, false);
    }

    @Override
    protected void flushStream() throws IOException {
        ctx.flush();
    }

    @Override
    protected void closeStream() throws IOException {
        ctx.close();
    }
}
