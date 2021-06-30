package com.sjq.live.support.original;

import com.sjq.live.model.LiveException;
import com.sjq.live.support.OutputStreamProcessor;
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
    protected void writeToStream(final byte[] bytes) {
        try {
            outputStream.write(bytes);
        } catch (IOException e){
            throw new LiveException(e);
        }
    }

    @Override
    protected void flushStream() {
        try {
            outputStream.flush();
        } catch (IOException e){
            throw new LiveException(e);
        }
    }

    @Override
    protected void closeStream() {
    }
}
