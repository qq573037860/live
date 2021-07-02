package com.sjq.live.support;

import com.sjq.live.utils.Queue;
import io.netty.util.concurrent.Future;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;
import java.util.Objects;

public abstract class OutputStreamProcessor {

    private final Queue<byte[]> queue = new Queue<>(10000);

    //private static Disruptor<ByteEvent> disruptor;
    //static {
        /*// Specify the size of the ring buffer, must be power of 2.
        int bufferSize = 1024;

        // Construct the Disruptor
        disruptor = new Disruptor<>(ByteEvent::new, bufferSize, DaemonThreadFactory.INSTANCE);

        // Connect the handler
        disruptor.handleEventsWith(new LongEventHandler());

        // Start the Disruptor, starts all threads running
        disruptor.start();

        // Get the ring buffer from the Disruptor to be used for publishing.
        RingBuffer<ByteEvent> ringBuffer = disruptor.getRingBuffer();

        ringBuffer.publishEvent((event, sequence, buffer) -> event.setBytes(buffer), "1,2,3".getBytes());*/
    //}
    /*public static class ByteEvent{
        private byte[] bytes;

        public byte[] getBytes() {
            return bytes;
        }

        public void setBytes(byte[] bytes) {
            this.bytes = bytes;
        }
    }

    public  static class LongEventFactory implements EventFactory<ByteEvent> {
        @Override
        public ByteEvent newInstance() {
            return new ByteEvent();
        }
    }

    public static class LongEventHandler implements EventHandler<ByteEvent>
    {
        public void onEvent(ByteEvent event, long sequence, boolean endOfBatch)
        {
            System.out.println("Event: " + new String(event.getBytes()));
        }
    }*/

    public void write(final byte[] bytes) {
        queue.offer(bytes);
    }

    public void close() {
        write(new byte[]{});
    }

    public void processData() {
        for (;;) {
            byte[] bytes = queue.poll(10L);
            if (Objects.isNull(bytes)) {
                continue;
            } else if (bytes.length == 0) {
                break;
            }
            writeToStream(bytes);
            flushStream();
        }
        closeStream();
    }

    protected abstract void writeToStream(byte[] bytes);

    protected abstract void flushStream();

    protected abstract void closeStream();

}
