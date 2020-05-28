package com.sjq.live.utils;

import java.util.concurrent.locks.LockSupport;

/**
 * 适用1p-1c场景 非线程安全
 * @param <T>
 */
public class Queue<T> {

    public static void main(String[] args) {
        //ConcurrentQueue<byte[]> queue = new ConcurrentQueue<>(100);
        Queue<Integer> queue = new Queue(1000);
        long st = System.currentTimeMillis();
        Thread p = new Thread(() -> {
            for (int i = 0; i < 1000; i++) {
                queue.offer(i);
            }
            queue.offer(-1);
            System.out.println("生产完毕");
        });
        Thread c = new Thread(() -> {
            for (;;) {
                int value = queue.poll();
                System.out.println("消费:" + value);
                if (-1 == value) {
                    System.out.println("消费完毕");
                    break;
                }
            }
        });
        p.start();
        c.start();
        try {
            p.join();
            c.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("耗时:" + (System.currentTimeMillis() - st));
    }

    final Object[] array;
    final int capacity;
    final int m;
    long tail;
    long head;
    final long[] als = new long[11];

    private static final int MIN_PARKTIME_NS = 10;
    private static final int MAX_PACKTIME_NS = 100;

    public Queue(int preferCapacity) {
        double pow = log2(Double.valueOf(preferCapacity));
        double intValuePow = (long)pow + 0.0;
        this.capacity = intValuePow == pow ? preferCapacity : Double.valueOf(Math.pow(2.0d, intValuePow + 1)).intValue();
        array = new Object[this.capacity];
        this.m = this.capacity - 1;

        for (int i = 0; i < als.length; i++) {
            als[i] = 0l;
        }
        head = als[3];
        tail = als[7];
    }

    public static Double log2(double N) {
        return Math.log(N)/Math.log(2);//Math.log的底为e
    }

    public boolean offer(T obj) {
        if(obj == null) throw new IllegalArgumentException("Can't put null object into this queue");
        int p =(int) (head++ & this.m);
        array[p] = obj;
        return true;
    }


    public T poll(){
        int p = (int) (tail++ & this.m);
        Object r;
        int parkTime = MIN_PARKTIME_NS;
        while((r = array[p]) == null) {
            LockSupport.parkNanos(parkTime);
            if(parkTime < MAX_PACKTIME_NS) parkTime <<= 1;
        }
        return (T) r;
    }

}
