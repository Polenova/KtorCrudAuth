package polenova.repository

import kotlinx.atomicfu.atomic
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import polenova.model.PostModel
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime

class PostRepositoryInMemoryWithMutexImpl : PostRepository {
    private var nextId = atomic(0L)
    private val items = mutableListOf<PostModel>()
    private val mutex = Mutex()

    override suspend fun getAll(): List<PostModel> =
        mutex.withLock {
            items.reversed()
        }

    override suspend fun getById(id: Long): PostModel? =
        mutex.withLock {
            items.find { it.id == id }
        }

    override suspend fun removeById(id: Long) {
        mutex.withLock {
            items.removeIf { it.id == id }
        }
    }

    override suspend fun save(item: PostModel): PostModel {
        mutex.withLock {
            return when (val index = items.indexOfFirst { it.id == item.id }) {
                -1 -> {
                    val dateCreated = LocalDateTime.now()
                    val dateId = ZoneId.of("Europe/Moscow")
                    val zonedDateTime = ZonedDateTime.of(dateCreated, dateId)
                    val copy = item.copy(id = nextId.incrementAndGet(), created = zonedDateTime)
                    items.add(copy)
                    copy
                }
                else -> {
                    items[index] = item
                    item
                }
            }
        }
    }

    override suspend fun likeById(id: Long): PostModel? {
        return when (val index = items.indexOfFirst { it.id == id }) {
            -1 -> {
                null
            }
            else -> {
                val item = items[index]
                val copy = item.copy(countLiked = item.countLiked + 1, likeByMe = true)
                items[index] = copy
                copy
            }
        }
    }

    override suspend fun commentById(id: Long, userId: Long): PostModel? {
        return when (val index = items.indexOfFirst { it.id == id }) {
            -1 -> null
            else -> {
                val item = items[index]
                val copy = item.copy(countComment = item.countComment+1)
                try {
                    items[index] = copy
                } catch (e: ArrayIndexOutOfBoundsException) {
                    println("size: ${items.size}")
                    println(index)
                }
                copy
            }
        }
    }

    override suspend fun shareById(id: Long, userId: Long): PostModel? {
        return when (val index = items.indexOfFirst { it.id == id }) {
            -1 -> null
            else -> {
                val item = items[index]
                val copy = item.copy(countShare = item.countShare+1)
                try {
                    items[index] = copy
                } catch (e: ArrayIndexOutOfBoundsException) {
                    println("size: ${items.size}")
                    println(index)
                }
                copy
            }
        }
    }

    override suspend fun getLastContent(): List<PostModel> {
        try {
            if (items.isEmpty()) {
                return emptyList()
            }
            return getAll().slice(0..4)
        } catch (e: IndexOutOfBoundsException) {
            return getAll()
        }    }

    override suspend fun getPostsAfter(id: Long): List<PostModel>? {
        val item = getById(id)
        val itemsReversed = getAll()
        return when (val index = itemsReversed.indexOfFirst { it.id == item?.id }) {
            -1 -> null
            0 -> emptyList()
            else -> itemsReversed.slice(0 until index)
        }    }

    override suspend fun getPostsBefore(id: Long): List<PostModel>? {
        val item = getById(id)
        val itemsReversed = getAll()
        return when (val index = itemsReversed.indexOfFirst { it.id == item?.id }) {
            -1-> null
            (items.size - 1) -> emptyList()
            else -> {
                try {
                    itemsReversed.slice((index + 1)..(index + 5))
                } catch (e: IndexOutOfBoundsException) {
                    itemsReversed.slice((index + 1) until items.size)
                }
            }
        }
    }
}



