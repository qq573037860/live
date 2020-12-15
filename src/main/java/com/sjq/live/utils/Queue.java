package com.sjq.live.utils;

import java.util.concurrent.CountDownLatch;

/**
 * 适用1p-1c场景 非线程安全
 *
 * @param <T>
 */
public class Queue<T> {

    private static final int MIN_PARK_TIME_NS = 10;
    private static final int MAX_PARK_TIME_NS = 160;
    final int capacity;
    final int m;
    volatile Object[] array;
    long p1, p2, p3, p4, p5, p6, p7;
    long tail;
    long p11, p12, p13, p14, p15, p16, p17;
    long head;
    long p21, p22, p23, p24, p25, p26, p27;
    //volatile long curReadIndex = 0L;
    //long p31, p32, p33, p34, p35, p36, p37;
    //volatile long curWriteIndex = 0L;
    //long p41, p42, p43, p44, p45, p46, p47;

    //final long[] als = new long[11];
    /*@Contended
    public final static class VolatileLong {

        public volatile long value = 0L;

        //public long p1, p2, p3, p4, p5, p6;

    }*/

    public Queue(int preferCapacity) {
        double pow = log2((double) preferCapacity);
        //取整数部分
        double longValuePow = (double) (long) pow;
        //容量取2的次方
        this.capacity = longValuePow == pow ? preferCapacity : Double.valueOf(Math.pow(2.0D, longValuePow + 1)).intValue();
        this.array = new Object[this.capacity];
        this.m = this.capacity - 1;

        //head = new VolatileLong();
        //tail = new VolatileLong();
        //head = als[3];
        //tail = als[7];
    }

    public static void main(String[] args) throws InterruptedException {
        long totalDuration = 0L;
        for (int i = 0; i < 10; i++) {
            totalDuration += doTest();
        }
        System.out.println("平均耗时：" + totalDuration / 10);
    }

    private static long doTest() throws InterruptedException {
        //ConcurrentQueue<Integer> queue = new ConcurrentQueue<>(1000);
        //ArrayBlockingQueue<Integer> queue = new ArrayBlockingQueue(1000);
        Queue<Integer> queue = new Queue<>(100);
        CountDownLatch countDownLatch = new CountDownLatch(2);
        long st = System.currentTimeMillis();
        StringBuilder pStr = new StringBuilder();
        Thread p = new Thread(() -> {
            for (int i = 0; i < 10000000; i++) {
                queue.offer(i);
                pStr.append(i);
            }
            queue.offer(-1);
            pStr.append(-1);
            System.out.println("生产完毕");
            countDownLatch.countDown();
        });
        StringBuilder cStr = new StringBuilder();
        Thread c = new Thread(() -> {
            for (; ; ) {
                Integer value = queue.poll();
                if (null == value) {
                    continue;
                }
                cStr.append(value);
                //System.out.println("消费:" + value);
                if (-1 == value) {
                    System.out.println("消费完毕");
                    break;
                }
            }
            countDownLatch.countDown();
        });
        p.start();
        c.start();

        countDownLatch.await();

        //System.out.println(pStr.toString());
        //System.out.println(cStr.toString());
        //System.out.println(pStr.toString().equals(cStr.toString()));
        System.out.println("耗时:" + (System.currentTimeMillis() - st));
        return System.currentTimeMillis() - st;
    }

    public static Double log2(double N) {
        return Math.log(N) / Math.log(2);//Math.log的底为e
    }

    public void offer(T obj) {
        if (obj == null) throw new IllegalArgumentException("Can't put null object into this queue");
        int p = (int) (head++ & this.m);

        //判断生产者是否套圈
        while (null != array[p]/*curWriteIndex - curReadIndex >= capacity*/) {
        }
        /*for (;;) {
            if (*//*null != array[p]*//* curWriteIndex - curReadIndex >= capacity) {
                //System.out.println("sleep");
                *//*try {
                    Thread.sleep(1l);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }*//*
            } else {
                if (array[p] != null) {
                    System.out.println("异常offer,curWriteIndex="
                            + curWriteIndex + ",curReadIndex="
                            + curReadIndex + ",diif="
                            + (curWriteIndex - curReadIndex) + ",%="
                            + ((curWriteIndex - curReadIndex)%capacity));
                }
                break;
            }
        }*/
        array[p] = obj;
        //curWriteIndex++;
    }

    public T poll() {
        int p = (int) (tail++ & this.m);
        //int parkTime = MIN_PARK_TIME_NS;
        Object r;
        while (/*curReadIndex >= curWriteIndex*/(r = array[p]) == null) {
            //LockSupport.parkNanos(parkTime);
            //if(parkTime < MAX_PARK_TIME_NS) parkTime <<= 1;
        }
        array[p] = null;
        //curReadIndex++;
        return (T) r;
    }
}
