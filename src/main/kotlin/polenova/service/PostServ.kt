package polenova.service

import io.ktor.features.NotFoundException
import io.ktor.util.KtorExperimentalAPI
import polenova.dto.PostRequestDto
import polenova.dto.PostResponseDto
import polenova.model.PostModel
import polenova.model.UserModel
import polenova.repository.PostRepository

class PostService(private val repo: PostRepository) {
    suspend fun getAll(userId: Long): List<PostResponseDto> {
        return repo.getAll().map { PostResponseDto.fromModel(it, userId) }
    }

    suspend fun getLastContent(userId: Long): List<PostResponseDto> {
        return repo.getLastContent().map { PostResponseDto.fromModel(it, userId) }
    }

    @KtorExperimentalAPI
    suspend fun getPostsAfter(id: Long, userId: Long): List<PostResponseDto> {
        val listPostsAfter = repo.getPostsAfter(id) ?: throw NotFoundException()
        return listPostsAfter.map { PostResponseDto.fromModel(it, userId) }
    }

    @KtorExperimentalAPI
    suspend fun getPostsBefore(id: Long, userId: Long): List<PostResponseDto> {
        val listPostsAfter = repo.getPostsBefore(id) ?: throw NotFoundException()
        return listPostsAfter.map { PostResponseDto.fromModel(it, userId) }
    }

    @KtorExperimentalAPI
    suspend fun getById(id: Long, userId: Long): PostResponseDto {
        val model = repo.getById(id) ?: throw NotFoundException()
        return PostResponseDto.fromModel(model, userId)
    }

    @KtorExperimentalAPI
    suspend fun shareById(id: Long, me: UserModel, input: PostRequestDto): PostResponseDto {
        val model = repo.shareById(id, me.id) ?: throw NotFoundException()
        val modelShared = PostModel(
            id = 0L,
            author = input.author,
            content = input.content,
            typePost = input.postType,
            sourceHTTP = input.sourceHTTP,
            user = me
        )
        return PostResponseDto.fromModel(repo.save(model), me.id)
    }

    @KtorExperimentalAPI
    suspend fun likeById(id: Long, userId: Long): PostResponseDto {
        val model = repo.likeById(id) ?: throw NotFoundException()
        return PostResponseDto.fromModel(model, userId)
    }

    suspend fun save(input: PostRequestDto, me: UserModel): PostResponseDto {
        val model = PostModel(id = input.id, author = input.author, content = input.content)
        return PostResponseDto.fromModel(repo.save(model), me.id)
    }

    @KtorExperimentalAPI
    suspend fun removeById(id: Long, me: UserModel): Boolean {
        val model = repo.getById(id) ?: throw NotFoundException()
        return if (model.user == me) {
            repo.removeById(id)
            true
        } else {
            false
        }
    }
}