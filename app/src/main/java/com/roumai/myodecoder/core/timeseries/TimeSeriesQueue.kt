package com.roumai.myodecoder.core.timeseries

import java.lang.IndexOutOfBoundsException

data class TimePoint(
    val timestamp: Long,
    val data: FloatArray,
    val loss: Boolean = false,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TimePoint

        if (timestamp != other.timestamp) return false
        if (!data.contentEquals(other.data)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = timestamp.hashCode()
        result = 31 * result + data.contentHashCode()
        return result
    }
}

fun mutableStateTimeSeriesQueueOf(size: Int, channelSize: Int = 1): TimeSeriesQueue {
    return TimeSeriesQueue(size, channelSize)
}

class TimeSeriesQueue(private val size: Int, private val channelSize: Int = 1) {
    private val queue = ArrayDeque<TimePoint>(size + 10) // 冗余 10 条记录，避免数据扩容拷贝
    private var topTimestamp = 0L
    private var currentSize = 0
    private var updater = 0L // state updater

    @Synchronized
    private fun append(index: Int, element: TimePoint): Boolean {
        val added = if (index < currentSize) {
//            println("Insert into $index, t=${element.timestamp}")
            queue.add(index, element)
            true
        } else {
//            println("Add into $index, t=${element.timestamp}")
            queue.add(element)
        }
        if (added) {
            updater++
            topTimestamp = element.timestamp
            if (currentSize >= size) {
                queue.removeFirstOrNull()
            } else {
                currentSize++
            }
            return true
        }
        return false
    }

    fun add(element: TimePoint): Boolean {
        if (element.timestamp > topTimestamp) {
            // put on top.
            return append(currentSize, element)
        }
        // duplicate?
        queue.indexOfFirst { it.timestamp == element.timestamp }.let {
            if (it != -1) {
                // replace
                queue[it] = element
                return true
            }
        }

        // rearrange
        val index = queue.indexOfFirst { it.timestamp > element.timestamp }
        if (index == -1) {
            return false
        }
        return append(index, element)
    }

    fun addAll(elements: Collection<TimePoint>): Boolean {
        return elements.sortedBy { it.timestamp }.all { add(it) }
    }

    @Synchronized
    fun clear() {
        queue.clear()
        currentSize = 0
        topTimestamp = 0L
        updater++
    }

    fun iterator(): MutableIterator<TimePoint> {
        return queue.iterator()
    }

    operator fun get(index: Int) = queue[index]

    fun toSubsequence(windowSize: Int): MutableList<TimePoint> {
        var start = currentSize - windowSize
        if (start < 0) start = 0
        return queue.subList(start, currentSize)
    }

    fun toSubsequence(
        expectTimestamp: Long,
        sampleInterval: Int,
        windowSize: Int
    ): MutableList<TimePoint> {
        if (topTimestamp < 100000L) return toSubsequence(windowSize)
        val recordNumber = (expectTimestamp - topTimestamp) / sampleInterval
        if (recordNumber < 0) return toSubsequence(windowSize) // 返回最新的数据，而不是以目标 timestamp 结束的数据
        var start = (currentSize - windowSize + recordNumber).toInt()
        if (start < 0) start = 0
        val left = queue.subList(start, currentSize)
        val zeros = floatArrayOf(0f)
//        println("start=$start, recordNumber=$recordNumber, left.size=${left.size}")
        val right = (0..recordNumber).map {
            TimePoint(
                expectTimestamp - (recordNumber - it) * sampleInterval,
                zeros
            )
        }
        return (left + right).toMutableList()
    }

    /**
     * 补齐，保证能获取 windowSize 个点，如果不足则补零，且间隙之间丢包的也补零
     */
    fun toFullTimeSeries(
        expectTimestamp: Long,
        sampleInterval: Long,
        windowSize: Int
    ): MutableList<TimePoint> {
        val expectTimestamp = queue.lastOrNull()?.timestamp ?: expectTimestamp
        val start = expectTimestamp - sampleInterval * windowSize
        val filtered = ArrayList<TimePoint>()
        try {
            val maxSize = TimeSeriesQueue@this.size
            with(queue) {
                var index = 0
                @Synchronized
                while (index < this.size && index < maxSize) {
                    val next = queue.getOrNull(index)
                    if (next != null) {
                        if (next.timestamp in start..expectTimestamp) {
                            filtered.add(next)
                        }
                    }
                    index++
                }
            }
        } catch (e: IndexOutOfBoundsException) {
            e.printStackTrace()
        }
        val data = filtered.associateBy { it.timestamp }
//        val data =
//            queue.filter { it.timestamp in start..expectTimestamp }.associateBy { it.timestamp }
        val zeros = (0..channelSize).map { 0f }.toFloatArray()
        return (start until expectTimestamp step sampleInterval).map {
            data[it] ?: TimePoint(it, zeros, true)
        }.toMutableList()
    }

    fun isEmpty(): Boolean {
        return queue.isEmpty()
    }

    fun size() = size

    /**
     * State 监听该 Queue 变化时的信号量
     */
    fun updaterIdentity(): Long {
        return updater
    }

    override fun toString(): String {
        return "TimeSeriesQueue(capacity=$size, size=$currentSize, timestamp=$topTimestamp, queue.size=${queue.size})"
    }
}

fun main() {
    val series = mutableStateTimeSeriesQueueOf(12)
    for (i in 0..100) {
        series.add(TimePoint(i.toLong() % 50, floatArrayOf(i.toFloat())))
    }
    println(series)
    println(series.updaterIdentity())
    println(series.size())
    series.toSubsequence(10).forEach {
        println(it)
    }
}