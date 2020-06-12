package polenova.service

import io.ktor.features.NotFoundException
import polenova.dto.PostRequestDto
import polenova.dto.PostResponseDto
import polenova.model.PostModel
import polenova.model.UserModel
import polenova.repository.PostRepository

class PostService(private val repo: PostRepository) {
    suspend fun getAll(): List<PostResponseDto> {
        return repo.getAll().map { PostResponseDto.fromModel(it) }
    }

    suspend fun getById(id: Long): PostResponseDto {
        val model = repo.getById(id) ?: throw NotFoundException()
        return PostResponseDto.fromModel(model)
    }

    suspend fun shareById(id: Long, me: UserModel, input: PostRequestDto): PostResponseDto {
        val model = repo.shareById(id) ?: throw NotFoundException()
        val modelShared = PostModel(
            id = 0L,
            author = input.author,
            content = input.content,
            typePost = input.postType,
            sourceHTTP = input.sourceHTTP,
            user = me
        )
        PostResponseDto.fromModel(repo.save(modelShared))
        return PostResponseDto.fromModel(model)
    }

    suspend fun likeById(id: Long): PostResponseDto {
        val model = repo.likeById(id) ?: throw NotFoundException()
        return PostResponseDto.fromModel(model)
    }

    suspend fun save(input: PostRequestDto): PostResponseDto {
        val model = PostModel(id = input.id, author = input.author, content = input.content)
        return PostResponseDto.fromModel(repo.save(model))
    }

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