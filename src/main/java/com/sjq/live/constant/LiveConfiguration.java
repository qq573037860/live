package com.sjq.live.constant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LiveConfiguration {

    @Value("${server.port}")
    private Integer serverPort;
    @Value("${server.extranet}")
    private String serverIp;

    private static final String ORIGIN_STREAM_PROTOCOL = "https";
    private static final String TRANSFORMED_STREAM_PROTOCOL = "https";
    private static final String ORIGIN_STREAM_PATH = "/originStream";
    private static final String TRANSFORMED_STREAM_PATH = "/transformedStream";

    public String buildOriginStreamUrl(final String publishId) {
        return String.format("%s://%s:%s%s?publishId=%s", ORIGIN_STREAM_PROTOCOL, serverIp, serverPort, ORIGIN_STREAM_PATH, publishId);
    }

    public String buildTransformedStreamUrl(final String publishId) {
        return String.format("%s://%s:%s%s?publishId=%s", TRANSFORMED_STREAM_PROTOCOL, serverIp, serverPort, TRANSFORMED_STREAM_PATH, publishId);
    }

    public Integer getServerPort() {
        return serverPort;
    }

    public String getServerIp() {
        return serverIp;
    }

    public static String getOriginStreamProtocol() {
        return ORIGIN_STREAM_PROTOCOL;
    }

    public static String getTransformedStreamProtocol() {
        return TRANSFORMED_STREAM_PROTOCOL;
    }

    public static String getOriginStreamPath() {
        return ORIGIN_STREAM_PATH;
    }

    public static String getTransformedStreamPath() {
        return TRANSFORMED_STREAM_PATH;
    }
}
