package dto

import model.PostModel
import model.TypePost

data class PostResponseDto(
    val id: Long,
    val author: String,
    val content: String?,
    val created: String,
    val location: Pair<Double, Double>? = null,

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
            source = model.source,
            sourceHTTP = model.sourceHTTP,
            likeByMe = model.likeByMe,
            countLiked = model.countLiked,
            countComment = model.countComment,
            countShare = model.countShare
        )
    }
}