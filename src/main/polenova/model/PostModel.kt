package model

import java.time.ZonedDateTime

data class PostModel(
    val id: Long,
    val author: String,
    val content: String? = null,
    val created: ZonedDateTime? = null,
    val location: Coordinate? = null,

    var typePost: TypePost = TypePost.POST,
    val sourceHTTP: String? = null,

    var likeByMe: Boolean = false,
    var countLiked: Int = 0,
    var countComment: Int = 0,
    var countShare: Int = 0
)

data class Coordinate (
    val latitude: String,
    val longitude: String
)

enum class TypePost {
    POST, REPOST, REPLY, VIDEO, COMMERCIAL
}