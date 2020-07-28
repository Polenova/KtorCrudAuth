package polenova.repository

import kotlinx.atomicfu.atomic
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import polenova.model.UserModel

class UserRepositoryInMemoryWithMutexImpl : UserRepository {
    private var nextId = atomic(0L)
    private val items = mutableListOf<UserModel>()
    private val mutex = Mutex()

    override suspend fun getAll(): List<UserModel> {
        mutex.withLock {
            return items.toList()
        }
    }

    override suspend fun getById(id: Long): UserModel? {
        mutex.withLock {
            return items.find { it.id == id }
        }
    }

    override suspend fun getByIds(ids: Collection<Long>): List<UserModel> {
        mutex.withLock {
            return items.filter { ids.contains(it.id) }
        }
    }

    override suspend fun getByUsername(username: String): UserModel? {
        mutex.withLock {
            return items.find { it.username == username }
        }
    }

    override suspend fun getByIdPassword(id: Long, password: String): UserModel? {
        val item = items.find { it.id == id }
        return if (password == item?.password) {
            item
        } else {
            null
        }
    }

    override suspend fun save(item: UserModel): UserModel {
        mutex.withLock {
            return when (val index = items.indexOfFirst { it.id == item.id }) {
                -1 -> {
                    val copy = item.copy(id = nextId.incrementAndGet())
                    items.add(copy)
                    copy
                }
                else -> {
                    val copy = items[index].copy(username = item.username, password = item.password)
                    items[index] = copy
                    copy
                }
            }
        }
    }

    override suspend fun saveFirebaseToken(id: Long, firebaseToken: String): UserModel? {
        return when (val index = items.indexOfFirst { it.id == id}) {
            -1 -> {
                null
            }
            else -> {
                val copy = items[index].copy(firebaseToken = firebaseToken)
                mutex.withLock {
                    items[index] = copy
                }
                copy
            }
        }
    }
}

