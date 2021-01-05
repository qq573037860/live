package com.sjq.live.utils;

import java.util.concurrent.CountDownLatch;

/**
 * 适用1p-1c场景 非线程安全
 * @param <T>
 */
public class Queue<T> {

    public static void main(String[] args) throws InterruptedException {
        long total = 0l;
        for (int i = 0; i < 10; i++) {
            total += test();
        }
        System.out.println("耗时：" + total/10);
    }

    private static long test() throws InterruptedException {
        //ConcurrentQueue<Integer> queue = new ConcurrentQueue<>(1000);
        //ArrayBlockingQueue<Integer> queue = new ArrayBlockingQueue(1000);
        Queue<Integer> queue = new Queue<>(1000);
        CountDownLatch countDownLatch = new CountDownLatch(2);
        long st = System.currentTimeMillis();
        StringBuilder pStr = new StringBuilder();
        Thread p = new Thread(() -> {
            for (int i = 0; i < 1000000; i++) {
                queue.offer(i);
                pStr.append(i);
            }
            queue.offer(-1);
            pStr.append(-1);
            //System.out.println("生产完毕");
            countDownLatch.countDown();
        });
        StringBuilder cStr = new StringBuilder();
        Thread c = new Thread(() -> {
            for (;;) {
                Integer value = queue.poll();
                if(null == value){
                    continue;
                }
                cStr.append(value);
                //System.out.println("消费:" + value);
                if (-1 == value) {
                    //System.out.println("消费完毕");
                    break;
                }
            }
            countDownLatch.countDown();
        });
        p.start();
        c.start();

        countDownLatch.await();

        return System.currentTimeMillis() - st;
    }


    final int capacity;
    final int m;
    volatile Object[] array;

/*    final VolatileLong tail;
    final VolatileLong head;*/
    long tail;
    long p11, p12, p13, p14, p15, p16, p17;
    long head;
    long p21, p22, p23, p24, p25, p26, p27;

/*    boolean isStart = true;

    volatile Boolean lock = false;*/

    private static final int MIN_PARK_TIME_NS = 10;
    private static final int MAX_PACK_TIME_NS = 160;


    public Queue(int preferCapacity) {
        double pow = log2(Double.valueOf(preferCapacity));
        double intValuePow = (long)pow + 0.0;
        this.capacity = intValuePow == pow ? preferCapacity : Double.valueOf(Math.pow(2.0d, intValuePow + 1)).intValue();
        array = new Object[this.capacity];
        this.m = this.capacity - 1;

        /*for (int i = 0; i < als.length; i++) {
            als[i] = 0l;
        }*/
        //head = new VolatileLong(0l);
        //tail = new VolatileLong(0l);
        //head = als[3];
        //tail = als[7];
    }

    public static Double log2(double N) {
        return Math.log(N)/Math.log(2);//Math.log的底为e
    }

    public boolean offer(T obj) {
        if(obj == null) throw new IllegalArgumentException("Can't put null object into this queue");
        int p =(int) (head++ & this.m);

        //判断生产者是否套圈（针对直播的场景，要求不是特别高，允许数据覆盖）
        /*for (;;) {
            if (null == array[p]) {
                break;
            } else {
                //LockSupport.parkNanos(MIN_PARKTIME_NS);
            }
        }*/

        array[p] = obj;
        return true;
    }

    public T poll() {
        int p = (int) (tail++ & this.m);
        Object r;
        //int parkTime = MIN_PARK_TIME_NS;
        while((r = array[p]) == null) {
            //LockSupport.parkNanos(parkTime);
            //if(parkTime < MAX_PACK_TIME_NS) parkTime <<= 1;
        }
        array[p] = null;
        return (T) r;
    }

    public T poll(long timeout) {
        int p = (int) (tail++ & this.m);
        Object r;
        long st = System.currentTimeMillis();
        while((r = array[p]) == null) {
            if (System.currentTimeMillis() - st >= timeout) {
                tail--;
                return null;
            }
        }
        array[p] = null;
        return (T) r;
    }
}
