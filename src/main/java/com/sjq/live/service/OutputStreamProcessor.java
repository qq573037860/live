package com.sjq.live.service;

import javax.servlet.ServletOutputStream;
import java.io.IOException;

public class OutputStreamProcessor {

    private Thread currentThread;
    private ServletOutputStream outputStream;

    private OutputStreamProcessor(){}

    public OutputStreamProcessor(Thread currentThread, ServletOutputStream outputStream) {
        this.currentThread = currentThread;
        this.outputStream = outputStream;
    }

    public static Builder getBuilder(){return new Builder();}

    public void wirte(byte[] bytes, boolean flush) throws IOException {
        outputStream.write(bytes);
        if (flush) {
            outputStream.flush();
        }
    }

    public void wirteAndFlush(byte[] bytes) throws IOException {
        wirte(bytes, true);
    }

    public static class Builder {

        private Thread currentThread;
        private ServletOutputStream outputStream;

        private Builder(){}

        public Builder currentThread(Thread currentThread) {
            this.currentThread = currentThread;
            return this;
        }

        public Builder outputStream(ServletOutputStream outputStream) {
            this.outputStream = outputStream;
            return this;
        }

        public OutputStreamProcessor build() {
            return new OutputStreamProcessor(this.currentThread, this.outputStream);
        }
    }

}
