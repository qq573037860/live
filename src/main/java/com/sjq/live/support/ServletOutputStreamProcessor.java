package com.sjq.live.support;

import org.springframework.util.Assert;

import javax.servlet.ServletOutputStream;
import java.io.IOException;

public class ServletOutputStreamProcessor extends OutputStreamProcessor {

    private final ServletOutputStream outputStream;

    public ServletOutputStreamProcessor(final ServletOutputStream outputStream) {
        Assert.notNull(outputStream, "ServletOutputStream不能为空");

        this.outputStream = outputStream;
    }

    @Override
    public void writeToStream(final byte[] bytes) throws IOException {
        outputStream.write(bytes);
    }

    @Override
    protected void flushStream() throws IOException {
        outputStream.flush();
    }

    @Override
    protected void closeStream() throws IOException {
        outputStream.close();
    }
}
