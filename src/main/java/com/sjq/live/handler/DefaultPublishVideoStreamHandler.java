package com.sjq.live.handler;

import com.sjq.live.model.OperateResponse;
import com.sjq.live.model.RequestParam;
import com.sjq.live.support.AbstractStreamDistributeHandler;
import com.sjq.live.support.PublishHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service("defaultPublishVideoStreamHandler")
public class DefaultPublishVideoStreamHandler implements VideoStreamHandler {

    @Autowired
    private TransformStreamHandler transformStreamHandler;

    @Override
    public void afterConnectionEstablished(RequestParam attribute, AbstractStreamDistributeHandler handler) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void afterConnectionEstablished(final RequestParam attribute) {
        //获取写入流句柄
        final OperateResponse<PublishHandler> operateResponse = transformStreamHandler.publish(attribute.getPublishId());
        if (operateResponse.isSuccess()) {
            attribute.setPublishHandler(operateResponse.getData());
        }
    }

    @Override
    public void handleBinaryMessage(final RequestParam attribute,
                                    final byte[] data) {
        //向管道中写入数据
        final PublishHandler writeHandler = attribute.getPublishHandler();
        writeHandler.write(data);
    }

    @Override
    public void afterConnectionClosed(final RequestParam attribute) {
        //关闭管道
        PublishHandler handler = attribute.getPublishHandler();
        if (Objects.nonNull(handler)) {
            handler.close();
        }
    }

}
