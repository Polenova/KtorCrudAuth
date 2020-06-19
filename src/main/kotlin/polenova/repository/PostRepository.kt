package polenova.repository

import polenova.model.PostModel

interface PostRepository {
    suspend fun getAll(): List<PostModel>
    suspend fun getById(id: Long): PostModel?
    suspend fun save(item: PostModel): PostModel
    suspend fun removeById(id: Long)
    suspend fun likeById(id: Long): PostModel?
    suspend fun commentById(id: Long, userId: Long): PostModel?
    suspend fun shareById(id: Long, userId: Long): PostModel?
    suspend fun getLastContent(): List<PostModel>
    suspend fun getPostsAfter(id: Long): List<PostModel>?
    suspend fun getPostsBefore(id: Long): List<PostModel>?
}