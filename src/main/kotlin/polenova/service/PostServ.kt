package polenova.service

import io.ktor.features.NotFoundException
import io.ktor.util.KtorExperimentalAPI
import polenova.dto.PostRequestDto
import polenova.dto.PostResponseDto
import polenova.exception.UserAccessException
import polenova.model.MediaModel
import polenova.model.MediaType
import polenova.model.PostModel
import polenova.model.UserModel
import polenova.repository.PostRepository

class PostService(private val repo: PostRepository) {
    suspend fun getAll(userId: Long): List<PostResponseDto> {
        return repo.getAll().map { PostResponseDto.fromModel(it, userId) }
    }

    suspend fun getRecent(userId: Long): List<PostResponseDto> {
        return repo.getRecent().map { PostResponseDto.fromModel(it, userId) }
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
    suspend fun likeById(id: Long, user: UserModel): PostResponseDto {
        val model = repo.likeById(id, user.id) ?: throw NotFoundException()
        val userOfPost = model.user!!
        return PostResponseDto.fromModel(model, user.id)
    }

    @KtorExperimentalAPI
    suspend fun dislikeById(id: Long, userId: Long): PostResponseDto {
        val model = repo.dislikeById(id, userId) ?: throw NotFoundException()
        return PostResponseDto.fromModel(model, userId)
    }

    @KtorExperimentalAPI
    suspend fun repostById(id: Long, me: UserModel, input: PostRequestDto): PostResponseDto {
        val model = repo.repostById(id, me.id) ?: throw NotFoundException()
        val modelShared = PostModel(
            id = 0L,
            textOfPost = "${input.textOfPost}\n........\nReposted text by ${model.user?.username}\n........\n${model.textOfPost}",
            postType = input.postType, address = input.address,
            sourceId = id,
            coordinates = input.coordinates, sourceVideo = input.sourceVideo, sourceAd = input.sourceAd, user = me,
            attachment = input.attachmentId?.let { MediaModel(id = it, mediaType = MediaType.IMAGE) }
        )
        PostResponseDto.fromModel(repo.save(modelShared), me.id)
        return PostResponseDto.fromModel(model, me.id)
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

    suspend fun save(input: PostRequestDto, me: UserModel): PostResponseDto {
        val model = PostModel(
            id = 0L,
            textOfPost = input.textOfPost,
            postType = input.postType, address = input.address,
            coordinates = input.coordinates, sourceVideo = input.sourceVideo, sourceAd = input.sourceAd, user = me,
            attachment = input.attachmentId?.let { MediaModel(id = it, mediaType = MediaType.IMAGE) }
        )
        return PostResponseDto.fromModel(repo.save(model), me.id)
    }

    @KtorExperimentalAPI
    suspend fun saveById(id: Long, input: PostRequestDto, me: UserModel): PostResponseDto {
        val model = PostModel(
            id = id,
            textOfPost = input.textOfPost,
            postType = input.postType, address = input.address,
            coordinates = input.coordinates, sourceVideo = input.sourceVideo, sourceAd = input.sourceAd, user = me,
            attachment = input.attachmentId?.let { MediaModel(id = it, mediaType = MediaType.IMAGE) }
        )
        val existingPostModel = repo.getById(id) ?: throw NotFoundException()
        if (existingPostModel.user?.id != me.id) {
            throw UserAccessException("Access denied, Another user posted this post")

        }
        return PostResponseDto.fromModel(repo.save(model), me.id)
    }
}