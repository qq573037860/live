package com.sjq.live.service;

import com.sjq.live.controller.TransformStreamManage;
import com.sjq.live.support.StreamWriteHandler;
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
public class PublishVideoStreamService extends AbstractBinaryWebSocketHandler {

    private static final Logger logger = LoggerFactory.getLogger(PublishVideoStreamService.class);

    private static final String path = "ws/publishVideoStream";

    @Autowired
    private TransformStreamManage manage;

    public PublishVideoStreamService() {
        super(path);
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        logger.info("client opened: " + session.toString());

        //获取写入流句柄
        Map<String, Object> map = session.getAttributes();
        map.put("streamHandler", manage.publish(map.get("publishId").toString()));

/*        FileInputStream in = new FileInputStream("D:\\BaiduNetdiskDownload\\01Python快速入门\\b_edit.mp4");
        byte[] bytes = new byte[1024];
        int len = 0;
        while ((len = in.read(bytes)) != -1) {
            handleBinaryMessage(session, new BinaryMessage(bytes, 0, len, true));
        }*/
    }

    /**
     * 主要用来接受二进制数据(视频流)。
     * @param session
     * @param message
     */
    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) {
        //向管道中写入数据
        StreamWriteHandler writeHandler = (StreamWriteHandler)session.getAttributes().get("streamHandler");
        writeHandler.write(message.getPayload().array());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        //关闭管道
        StreamWriteHandler handler = (StreamWriteHandler) session.getAttributes().get("streamHandler");
        if (null != handler) {
            handler.close();
        }
        logger.info("client onclose：" + session.toString());
    }
}
