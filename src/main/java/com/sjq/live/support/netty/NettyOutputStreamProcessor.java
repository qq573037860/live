package com.sjq.live.support.netty;

import com.sjq.live.model.NettyHttpContext;
import com.sjq.live.support.OutputStreamProcessor;
import com.sjq.live.utils.NettyUtils;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.util.Assert;

public class NettyOutputStreamProcessor extends OutputStreamProcessor {

    private final ChannelHandlerContext ctx;

    public NettyOutputStreamProcessor(final NettyHttpContext request) {
        Assert.notNull(request, "NettyRequest不能为空");

        this.ctx = request.getCtx();
        //返回一个HttpChunk的开头
        NettyUtils.writeHttpChunkResponse(ctx);
    }

    @Override
    public void writeToStream(final byte[] bytes) {
        //返回HttpChunkContent
        NettyUtils.writeChunkContentResponse(ctx, bytes);
    }

    @Override
    public void flushStream() {
        ctx.flush();
    }

    @Override
    protected void closeStream() {
        NettyUtils.writeLastEmptyContentResponse(ctx);
        ctx.flush();
        ctx.close();
    }
}
