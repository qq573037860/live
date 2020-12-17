package com.sjq.live.utils.media.ffmepg;

import com.sjq.live.model.LiveException;
import com.sjq.live.utils.media.StreamConverter;
import com.sjq.live.utils.media.StreamConverterHandler;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(value = "stream.converter", havingValue = "ffmpeg")
public class FFmpegStreamConverter implements StreamConverter {

    @Override
    public StreamConverterHandler startConvert(String originStreamUrl, String transformedStreamUrl)  throws LiveException {
        //开启ffmpeg视频流转换进程，ffmpeg会调用originStream读取原始视频流，然后调用transformedStream输出转换后的视频流
        try {
            final Process process = FfmpegUtil.convertStream(originStreamUrl, transformedStreamUrl);
            return new FFmpegStreamConverterHandler(process);
        } catch (FFmpegException e) {
            throw new LiveException("视频流转换失败：", e);
        }
    }
}
