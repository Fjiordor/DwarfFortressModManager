package Crawler

import java.io.IOException
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

class Crawler(private val threadCount: Int = 20, private val debugCrawler: Boolean = false,
              private val debugWrapper: Boolean = false, private val retryThreshold: Int = 3) {

    private var kill = false
    private var unrecoverableErrorCount = 0
    private var ioErrorCount = 0

    init {
        if (debugCrawler) println("Starting Thread Pool with $threadCount threads. Debug enabled.")
    }

    private val workers = ThreadPoolExecutor(threadCount, threadCount, 0L, TimeUnit.MILLISECONDS, LinkedBlockingQueue())

    /**
     * Uniqueness is guaranteed via T.hashCode and T.equals
     */
    fun queue(task: Crawler.Crawler.Task) = queueRunnable( Runnable {
        task.execute(debugWrapper) { it -> queueRunnable(it, 0)}
    }, 0)

    fun awaitEnd() {
        //Wait until all threads finished executing
        while (!(workers.queue.isEmpty() && workers.activeCount == 0)) {
            if (debugCrawler) println("Queued: ${workers.queue.size} Active: ${workers.activeCount} IO Errors: $ioErrorCount Unrecoverable: $unrecoverableErrorCount")
            if (kill) workers.shutdownNow()
            Thread.sleep(500)
        }
        workers.shutdown()
        workers.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS)
    }

    private fun queueRunnable(runnable: Runnable, retry: Int): Unit = workers.execute {
        try {
            runnable.run()
        } catch (ioe: IOException) {
            if (debugCrawler) println("'${ioe.localizedMessage}' caught.")
            if (debugCrawler) println("Detected IOException.")
            synchronized(this.ioErrorCount) {
                this.ioErrorCount++
            }
            if (retry < retryThreshold) {
                if (debugCrawler) println("Failure #${retry + 1}. Resubmitting to queue.")
                queueRunnable(runnable, retry + 1)
            } else {
                println("Runnable crossed retry threshold, considered unrecoverable")
                synchronized(this.unrecoverableErrorCount) {
                    this.unrecoverableErrorCount++
                }
            }
        } catch (e: Throwable) {
            println("Caught Exception from crawler thread:")
            e.printStackTrace()
            synchronized(this.unrecoverableErrorCount) {
                this.unrecoverableErrorCount++
            }
        }
    }


    interface Task {
        fun execute(debug: Boolean, executor: (Runnable) -> Unit)
    }
}