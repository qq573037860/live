package com.sjq.live.service;

import javax.servlet.ServletOutputStream;
import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;

public class OutputStreamProcessor {

    private ServletOutputStream outputStream;
    private final ConcurrentLinkedQueue<byte[]> queue = new ConcurrentLinkedQueue();

    //private static Disruptor<ByteEvent> disruptor;

    static {
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
    }
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

    private OutputStreamProcessor(ServletOutputStream outputStream) {
        this.outputStream = outputStream;
    }

    public static Builder getBuilder(){return new Builder();}

    public void write(byte[] bytes) throws IOException {
        queue.add(bytes);
    }


    public void writeToEnd() throws IOException {
        write(new byte[]{});
    }

    public void waitingForData() throws IOException {
        for (;;) {
            byte[] bytes = queue.poll();
            if (null == bytes) {
                continue;
            } else if (bytes.length == 0) {
                break;
            }
            outputStream.write(bytes);
            outputStream.flush();
        }
        outputStream.close();
    }

    public static class Builder {

        private ServletOutputStream outputStream;

        private Builder(){}

        public Builder outputStream(ServletOutputStream outputStream) {
            this.outputStream = outputStream;
            return this;
        }

        public OutputStreamProcessor build() {
            return new OutputStreamProcessor(this.outputStream);
        }
    }

}
