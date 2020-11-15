package com.mountains.bledemo

import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock

fun main(args: Array<String>) {
    val testClass = TestClass3()
    val threadPoolExecutor = ThreadPoolExecutor(1, 1, 0, TimeUnit.NANOSECONDS, LinkedBlockingQueue())
    threadPoolExecutor.execute(TestClass3())
}


internal class TestClass3 : Runnable {

    private val readLock = ReentrantLock()
    private val  condition= readLock.newCondition()
    override fun run() {
        synchronized(readLock){
            readLock.lock()
            Thread(Runnable {
                Thread.sleep(6000)
                condition.signal()
            }).start()
            condition.await()
            println("1234")
            readLock.unlock()
        }


    }
}