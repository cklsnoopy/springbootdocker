package com.example.springbootdocker;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

//计数器 限流
public class CounterLimiter {
    //起始时间
    private static long startTime = System.currentTimeMillis();
    //时间间隔1000ms
    private static long interval = 1000;
    //每个时间间隔内，限制数量
    private static long limit = 3;
    //累加器
    private static AtomicLong accumulator = new AtomicLong();

    /**
     * true 代表放行，请求可已通过
     * false 代表限制，不让请求通过
     */
    public static boolean tryAcquire() {
        long nowTime = System.currentTimeMillis();
        //判断是否在上一个时间间隔内
        if (nowTime < startTime + interval) {
            //如果还在上个时间间隔内
            long count = accumulator.incrementAndGet();
            if (count <= limit) {
                return true;
            } else {
                return false;
            }
        } else {
            //如果不在上一个时间间隔内
            synchronized (CounterLimiter.class) {
                //防止重复初始化
                if (nowTime > startTime + interval) {
                    startTime = nowTime;
                    accumulator.set(0);
                }
            }
            //再次进行判断
            long count = accumulator.incrementAndGet();
            if (count <= limit) {
                return true;
            } else {
                return false;
            }
        }
    }

    public static void main(String[] args) {
        //线程池，用于多线程模拟测试
        ExecutorService pool = Executors.newFixedThreadPool(10);
        // 被限制的次数
        AtomicInteger limited = new AtomicInteger(0);
        // 线程数
        final int threads = 10;
        // 每条线程的执行轮数
        final int turns = 1;
        // 同步器
        CountDownLatch countDownLatch = new CountDownLatch(threads);
        long start = System.currentTimeMillis();
        for (int i = 0; i < threads; i++) {

            pool.submit(() ->
            {
                 try{
                     for(int j = 0;j<turns;j++){
                         boolean flag = tryAcquire();
                         if(!flag){
                             limited.getAndIncrement();
                         }
                         Thread.sleep(200);
                     }
                 }catch (Exception e){
                     e.printStackTrace();
                 }
                //等待所有线程结束
                countDownLatch.countDown();
        });
        }
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        float time = (System.currentTimeMillis() - start) / 1000F;
        System.out.println("限制的次数为：" + limited.get() +",通过的次数为：" + (threads * turns - limited.get()));
        System.out.println("限制的比例为：" + (float) limited.get() / (float) (threads * turns));
        System.out.println("运行的时长为：" + time + "s");
    }

}
