package com.sjq.live.utils;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.concurrent.locks.LockSupport;

/**
 * 线程安全队列
 * @param <T>
 */
public class ConcurrentQueue<T> {

    public static void main(String[] args) {
        //ConcurrentQueue<byte[]> queue = new ConcurrentQueue<>(100);
        ConcurrentLinkedQueue<byte[]> queue = new ConcurrentLinkedQueue();
        byte[] temp = new byte[1000];
        for (int i = 0; i < temp.length; i++) {
            temp[i] = (byte)i;
        }
        long st = System.currentTimeMillis();
        Thread p = new Thread(() -> {
            for (int i = 0; i < 100000000; i++) {
                queue.offer(temp);
            }
            queue.offer(new byte[]{});
            System.out.println("生产完毕");
        });
        Thread c = new Thread(() -> {
            for (;;) {
                byte[] bytes = queue.poll();
                if (null != bytes && bytes.length == 0) {
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

    private final byte[] falseOffer;   //新增
    private final byte[] falsePoll;  //新增
    private final AtomicReferenceArray<T> array;
    private final int capacity;
    private final int m;
    private final AtomicLong tail;
    private final AtomicLong head;
    private final AtomicLong[] als = new AtomicLong[11];

    private static final int INTERNAL_PACK_COUNT = 100;

    public ConcurrentQueue(int preferCapacity) {
        double pow = log2(Double.valueOf(preferCapacity));
        double intValuePow = (long)pow + 0.0;
        this.capacity = intValuePow == pow ? preferCapacity : Double.valueOf(Math.pow(2.0d, intValuePow + 1)).intValue();
        array = new AtomicReferenceArray(this.capacity);
        falseOffer = new byte[this.capacity];
        falsePoll = new byte[this.capacity];
        this.m = this.capacity - 1;

        for (int i = 0; i < als.length; i++) {
            als[i] = new AtomicLong(0);
        }
        head = als[3];
        tail = als[7];
    }

    public static Double log2(double N) {
        return Math.log(N)/Math.log(2);//Math.log的底为e
    }

    public boolean offer(T obj) {
        if(obj == null) throw new IllegalArgumentException("Can't put null object into this queue");
        for (;;) {
            long head = this.head.get();
            int p =(int) (head & this.m);
            if(falsePoll[p] > 0) {
                synchronized(falsePoll) {  //运行比例很低，性能要求不高，直接同步处理
                    if(falsePoll[p] > 0) {  //如果不满足条件，说明失效计数已被其他线程处理，break; 回到最初重新尝试offer
                        if(this.head.compareAndSet(head, head + 1)){ //如果不满足条件，说明位置P已经失效，回到最初重新尝试offer
                            falsePoll[p] --; //跳过一次存在poll失效计数的位置p, poll失效计数 - 1，回到最初重新尝试offer
                        }
                    }
                }
                break;
            }
            if(array.get(p) != null) return false;
            if(this.head.compareAndSet(head, head + 1)) {
                for(int i = 0; i < INTERNAL_PACK_COUNT; i ++) {
                    if(!array.compareAndSet(p, null, obj)) {
                        LockSupport.parkNanos(2 << i);
                    } else return true;
                }
                synchronized(falseOffer) {  //运行比例很低，性能要求不高，直接同步处理
                    falseOffer[p] ++;  //位置p的add失效计数器
                }
            }
            return false;
        }
        return false;
    }


    public T poll(){
        for (;;) {
            T r;
            long tail = this.tail.get();
            int p = (int) (tail & this.m);
            if(falseOffer[p] > 0) {
                synchronized(falseOffer) {
                    if(this.tail.compareAndSet(tail, tail + 1)) {
                        falseOffer[p]--;
                    }
                }
                break;
            }
            r = array.get(p);
            if(r == null) return null;
            if(this.tail.compareAndSet(tail, tail + 1)) {
                for(int i = 0; i < INTERNAL_PACK_COUNT; i ++) {
                    if((r = array.getAndSet(p, null)) == null){
                        LockSupport.parkNanos(2 << i);
                    } else return r;
                }
                synchronized(falsePoll) {
                    falsePoll[p] ++;
                }
            }
            return null;
        }
        return null;
    }

}
