package polenova.model

import java.time.ZonedDateTime

data class PostModel(
    val id: Long,
    val textOfPost: String? = null,
    val dateOfPost: ZonedDateTime? = null,
    val postType: PostType = PostType.POST,
    val sourceId: Long? = null,
    val address: String? = null,
    val coordinates: Coordinates? = null,
    val sourceVideo: String? = null,
    val sourceAd: String? = null,
    val user: UserModel? = null,
    val attachment: MediaModel? = null,
    var likedUserIdList: MutableList<Long> = mutableListOf(),
    var repostedUserIdList: MutableList<Long> = mutableListOf()
)

data class Coordinates(
    val longitude: String,
    val latitude: String
)

enum class PostType {
    POST, REPOST, REPLY, VIDEO, COMMERCIAL
}