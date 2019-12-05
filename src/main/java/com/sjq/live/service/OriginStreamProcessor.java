package com.sjq.live.service;

import com.sjq.live.controller.TransformStreamManage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Map;

/**
 * Created by shenjq on 2019/12/2
 */

@Component
public class OriginStreamProcessor extends TextWebSocketHandler {

    private static final Logger logger = LoggerFactory.getLogger(OriginStreamProcessor.class);


    @Autowired
    private TransformStreamManage manage;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        logger.info("client opened: " + session.toString());
    }

    /**
     * 主要用来接受二进制数据(视频流)。
     * @param session
     * @param message
     */
    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) {
        Map<String, Object> map = session.getAttributes();
        Object handler = map.get("streamHandler");
        if (null == handler) {
            map.put("streamHandler", "init");
            //异步设置streamPipeline，可能会存在数据感知不及时的问题，但对于直播的场景而言问题不大
            new Thread(() -> {
                session.getAttributes().put("streamHandler", manage.publish(map.get("publishId").toString()));
            }).start();
        } else if (handler instanceof TransformStreamManage.StreamWriteHandler) {
            //向管道中写入数据
            TransformStreamManage.StreamWriteHandler writeHandler = (TransformStreamManage.StreamWriteHandler)handler;
            if (message.isLast()) {
                writeHandler.writeWithFlush(message.getPayload().array());
            } else {
                writeHandler.write(message.getPayload().array());
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        //关闭管道
        Object handler = session.getAttributes().get("streamHandler");
        if (null != handler && handler instanceof TransformStreamManage.StreamWriteHandler) {
            ((TransformStreamManage.StreamWriteHandler)handler).close();
        }
        logger.info("client onclose：" + session.toString());
    }
}
