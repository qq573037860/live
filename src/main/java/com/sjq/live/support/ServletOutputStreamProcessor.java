package com.sjq.live.support;

import javax.servlet.ServletOutputStream;
import java.io.IOException;

public class ServletOutputStreamProcessor extends OutputStreamProcessor {

    private ServletOutputStream outputStream;

    public ServletOutputStreamProcessor(ServletOutputStream outputStream) {
        this.outputStream = outputStream;
    }

    @Override
    public void writeToStream(byte[] bytes) throws IOException {
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
