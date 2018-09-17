package Crawler

import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

class DatabaseWriteQueue(private val access: Crawler.DatabaseAccess) {
    private val executor = ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, LinkedBlockingQueue())

    fun scheduleWrite(work: Crawler.Tag) = executor.execute {
        //access.writeTag()
    }

    fun awaitEnd() {
        while(!(executor.queue.isEmpty() && executor.activeCount == 0)) {
            println("Queued: ${executor.queue.size} Active: ${executor.activeCount}")
            Thread.sleep(500)
        }
        executor.shutdown()
        executor.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS)
    }
}