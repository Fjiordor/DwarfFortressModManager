package Crawler

import java.util.*

abstract class Tag(val name: String) {
    private val children = LinkedList<Crawler.Tag>()

    fun addChild(tag: Crawler.Tag) = children.add(tag)

    fun childIterator() = children.iterator()

    fun getLastChild() = children.last

    override fun toString() = "tag:$name"
}