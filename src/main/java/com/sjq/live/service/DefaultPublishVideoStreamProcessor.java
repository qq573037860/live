package com.sjq.live.service;

import com.sjq.live.model.OperateResponse;
import com.sjq.live.model.WebSocketAttribute;
import com.sjq.live.support.PublishHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class DefaultPublishVideoStreamProcessor implements PublishVideoStreamProcessor {

    @Autowired
    private TransformStream transformStream;

    @Override
    public void afterConnectionEstablished(final WebSocketAttribute attribute) {
        //获取写入流句柄
        final OperateResponse<PublishHandler> operateResponse = transformStream.publish(attribute.getPublishId());
        if (operateResponse.isSuccess()) {
            attribute.setPublishHandler(operateResponse.getData());
        }
    }

    @Override
    public void handleBinaryMessage(final WebSocketAttribute attribute,
                                    final byte[] data) {
        //向管道中写入数据
        final PublishHandler writeHandler = attribute.getPublishHandler();
        writeHandler.write(data);
    }

    @Override
    public void afterConnectionClosed(final WebSocketAttribute attribute) {
        //关闭管道
        PublishHandler handler = attribute.getPublishHandler();
        if (Objects.nonNull(handler)) {
            handler.close();
        }
    }

}
