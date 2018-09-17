package Crawler

import java.io.File

fun main(args: Array<String>) {
    Crawler.crawlAll(args)
}

fun crawlAll(args: Array<String>) {
    val objectFolderName = args.joinToString(separator = " ")
    val folder = File(objectFolderName)
    val crawler = Crawler.Crawler.Crawler(debugCrawler = true)
    crawler.queue (object: Crawler.Crawler.Crawler.Task {
        override fun execute(debug: Boolean, executor: (Runnable) -> Unit) {
            folder.listFiles().filter { it.isFile && it.name.endsWith(".txt") }.forEach {
                try {
                    executor.invoke(Crawler.FileHandler(it))
                } catch (e: Exception) {
                    println(it.absolutePath)
                    throw e
                }
            }
        }
    })
    crawler.awaitEnd()
}