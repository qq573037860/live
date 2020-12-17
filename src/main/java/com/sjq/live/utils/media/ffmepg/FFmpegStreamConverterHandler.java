package com.sjq.live.utils.media.ffmepg;

import com.sjq.live.utils.media.StreamConverterHandler;

public class FFmpegStreamConverterHandler implements StreamConverterHandler {

    private final Process process;

    public FFmpegStreamConverterHandler(final Process process) {
        this.process = process;
    }

    @Override
    public void destory() {
        process.destroyForcibly();
    }

}
