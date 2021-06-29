package com.sjq.live.constant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LiveConfiguration {

    @Value("${server.port}")
    private Integer serverPort;
    @Value("${server.extranet}")
    private String serverIp;

    @Value("${server.ssl.key-password}")
    private String sslKeyPassword;

    private static final String ORIGIN_STREAM_PROTOCOL = "https";
    private static final String TRANSFORMED_STREAM_PROTOCOL = "https";
    public static final String ORIGIN_STREAM_PATH = "/originStream";
    public static final String TRANSFORMED_STREAM_PATH = "/transformedStream";
    public static final String WEBSOCKET_PROTOCOL = "wss";
    public static final String PUBLISH_PATH = "/ws/publishVideoStream";
    public static final String SUBSCRIBE_PATH = "/ws/subscribeVideoStream";

    public String buildOriginStreamUrl(final String publishId) {
        return String.format("%s://%s:%s%s?publishId=%s", ORIGIN_STREAM_PROTOCOL, serverIp, serverPort, ORIGIN_STREAM_PATH, publishId);
    }

    public String buildTransformedStreamUrl(final String publishId) {
        return String.format("%s://%s:%s%s?publishId=%s", TRANSFORMED_STREAM_PROTOCOL, serverIp, serverPort, TRANSFORMED_STREAM_PATH, publishId);
    }

    public String buildWebsocketPublishUrl() {
        return String.format("%s://%s:%s%s", WEBSOCKET_PROTOCOL, serverIp, serverPort, PUBLISH_PATH);
    }

    public String buildWebsocketSubscribeUrl() {
        return String.format("%s://%s:%s%s", WEBSOCKET_PROTOCOL, serverIp, serverPort, SUBSCRIBE_PATH);
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

    public String getSslKeyPassword() {
        return sslKeyPassword;
    }
}
