package com.mountains.bledemo;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class Testlock {

    public static void main(String[] args){
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(1, 1, 0, TimeUnit.DAYS, new LinkedBlockingQueue<Runnable>());
        TestClass testClass = new TestClass();
        threadPoolExecutor.execute(testClass);
    }



//    private static void test(){
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                readLock.lock();
//                System.out.println("1234");
//                try {
//                    Thread.sleep(5000);
//                    readLock.unlock();
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
//        }).start();
//    }
//
//    private static void test2(){
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                readLock.lock();
//                System.out.println("1234");
//            }
//        }).start();
//    }
}

class TestClass implements Runnable{

    private ReentrantLock readLock = new ReentrantLock();
    private Condition condition = readLock.newCondition();

    @Override
    public void run() {
            readLock.lock();

            new Thread(new Runnable() {
                @Override
                public void run() {
                   /* try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }*/
                    System.out.println("等待");
                    readLock.lock();
                    condition.signal();
                    readLock.unlock();


                    System.out.println("5678");
                }
            }).start();

            try {
                Thread.sleep(2000);
               // boolean await = condition.await(4, TimeUnit.SECONDS);
                //System.out.println("result:"+await);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("1234");

            readLock.unlock();
            System.out.println("解锁");


    }

}