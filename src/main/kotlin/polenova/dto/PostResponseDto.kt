package polenova.dto

import polenova.model.Coordinate
import polenova.model.PostModel
import polenova.model.TypePost
import java.time.ZonedDateTime

data class PostResponseDto(
    val id: Long,
    val author: String,
    val content: String?,
    val created: ZonedDateTime? = null,
    val location: Coordinate? = null,

    var typePost: TypePost = TypePost.POST,
    val source: PostModel? = null,
    val sourceHTTP: String? = null,

    var likeByMe: Boolean = false,
    var countLiked: Int = 0,
    var countComment: Int = 0,
    var countShare: Int = 0
) {
    companion object {
        fun fromModel(model: PostModel) = PostResponseDto(
            id = model.id,
            author = model.author,
            content = model.content,
            created = model.created,
            location = model.location,
            typePost = model.typePost,
            sourceHTTP = model.sourceHTTP,
            likeByMe = model.likeByMe,
            countLiked = model.countLiked,
            countComment = model.countComment,
            countShare = model.countShare
        )
    }
}