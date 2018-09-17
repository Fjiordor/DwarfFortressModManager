package Crawler

import java.io.File
import java.io.FileReader
import java.util.*

class FileHandler(val file: File): Runnable {

    private lateinit var objectType: String
    private val stack = Stack<Crawler.Tag>()
    private val results = LinkedList<Crawler.Tag>()

    private val flagRegex = Regex("\\[([^:\\[]*)]")
    private val pairRegex = Regex("\\[([^:\\[]*):([^\\[]*)]")
    private val objectRegex = Regex("\\[OBJECT:([^\\[]*)]")
    private val tabRegex = Regex("^(\t*)")

    private var currentLine = 0

    override fun run() {
        try {
            parseFile()
        } catch (e: Exception) {
            println("An error occured in file ${file.absolutePath} on line $currentLine: ${e.javaClass.name}")
        }
    }

    private fun parseFile() {
        val fileReader = FileReader(file)
        val lines = fileReader.readLines().iterator()
        do {
            currentLine++
            val match = objectRegex.matchEntire(lines.next())
            if(match != null) {
                objectType = match.groupValues[1]
            }
        } while (match == null)
        lines.forEachRemaining { line ->
            currentLine++
            val indentation = getIndentation(line)
            val tags = getTags(line)
            if(tags.size == 0) return@forEachRemaining
            while (stack.size > indentation) {
                val popped = stack.pop()
                if (stack.size == 0) {
                    results.add(popped)
                }
            }
            if(stack.size < indentation) {
                stack.push(stack.peek().getLastChild())
            }
            if(stack.size == indentation) {
                if (stack.size == 0 && tags.size == 1) {
                    stack.push(tags[0])
                } else {
                    tags.forEach { stack.peek().addChild(it) }
                }
            }
        }
        fileReader.close()
    }

    private fun getTags(line: String): LinkedList<Crawler.Tag> {
        val ret = LinkedList<Crawler.Tag>()
        pairRegex.findAll(line).forEach {
            ret.add(Crawler.Attribute(it.groupValues[1], it.groupValues[2]))
        }
        flagRegex.findAll(line).forEach {
            ret.add(Crawler.Flag(it.groupValues[1]))
        }
        return ret
    }

    private fun getIndentation(line: String) = tabRegex.find(line)!!.groups[1]!!.value.length
}