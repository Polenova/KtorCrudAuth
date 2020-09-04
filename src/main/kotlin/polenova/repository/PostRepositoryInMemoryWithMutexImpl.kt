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

    override suspend fun getAll(): List<PostModel> = items.reversed()

    override suspend fun getRecent(): List<PostModel> {
        try {
            if (items.isEmpty()) {
                return emptyList()
            }
            return getAll().slice(0..4)
        } catch (e: IndexOutOfBoundsException) {
            return getAll()
        }
    }

    override suspend fun getPostsAfter(id: Long): List<PostModel>? {
        val item = getById(id)
        val itemsReversed = getAll()
        return when (val index = itemsReversed.indexOfFirst { it.id == item?.id }) {
            -1 -> null
            0 -> emptyList()
            else -> itemsReversed.slice(0 until index)
        }
    }

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
    override suspend fun getById(id: Long): PostModel? = items.find { it.id == id }


    override suspend fun save(item: PostModel): PostModel =
        when (val index = items.indexOfFirst { it.id == item.id }) {
            -1 -> {
                val todayDate = LocalDateTime.now()
                val dateId = ZoneId.of("Europe/Moscow")
                val zonedDateTime = ZonedDateTime.of(todayDate, dateId)
                val copy = item.copy(id = nextId.incrementAndGet(), dateOfPost = zonedDateTime)
                mutex.withLock {
                    items.add(copy)
                }
                copy
            }
            else -> {
                mutex.withLock {
                    items[index] = item
                }
                item
            }
        }

    override suspend fun removeById(id: Long) {
        mutex.withLock {
            items.removeIf { it.id == id }
        }
    }

    override suspend fun likeById(id: Long, userId: Long): PostModel? {
        val index = items.indexOfFirst { it.id == id }
        if (index < 0) return null
        mutex.withLock {
            items[index].likedUserIdList.add(userId)
        }
        return items[index]
    }

    override suspend fun dislikeById(id: Long, userId: Long): PostModel? {
        val index = items.indexOfFirst { it.id == id }
        if (index < 0) return null
        mutex.withLock {
            items[index].likedUserIdList.remove(userId)
        }
        return items[index]
    }

    override suspend fun repostById(id: Long, userId: Long): PostModel? {
        val index = items.indexOfFirst { it.id == id }
        if (index < 0) return null
        mutex.withLock {
            items[index].repostedUserIdList.add(userId)
        }
        return items[index]
    }
}

