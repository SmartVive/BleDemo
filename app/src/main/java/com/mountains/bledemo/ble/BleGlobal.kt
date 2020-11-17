package com.mountains.bledemo.ble

import com.mountains.bledemo.ble.callback.CommCallBack
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock

object BleGlobal {
    //ble通信不能并发，所以使用线程池和ReentrantLock来保证串行
    val bleCommThreadPoll: ThreadPoolExecutor = ThreadPoolExecutor(1, 1, 0, TimeUnit.NANOSECONDS, LinkedBlockingQueue())
    val lock = ReentrantLock()
    val condition = lock.newCondition()
    val commCallbackMap  = hashMapOf<String, CommCallBack>()
}