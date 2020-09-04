package polenova.dto

import polenova.model.*
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class PostResponseDto(
    val id: Long,
    val textOfPost: String? = null,
    val dateOfPost: String? = null,
    val nameAuthor: String?,
    var repostsCount: Int,
    var likesCount: Int,
    var isLikedByUser: Boolean,
    var isRepostedByUser: Boolean,
    val postType: PostType = PostType.POST,
    val sourceId: Long? = null,
    val address: String? = null,
    val coordinates: Coordinates? = null,
    val sourceVideo: String? = null,
    val sourceAd: String? = null,
    val attachmentId: String? = null
) {
    companion object {
        fun fromModel(postModel: PostModel, userId: Long): PostResponseDto {
            val isLikedByUser = postModel.likedUserIdList.contains(userId)
            val isRepostedByUser = postModel.repostedUserIdList.contains(userId)
            val likesCount = postModel.likedUserIdList.size
            val repostsCount = postModel.repostedUserIdList.size

            val formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy - HH:mm:ss Z");
            val dateOfPostString = postModel.dateOfPost?.format(formatter)

            return PostResponseDto(
                id = postModel.id,
                textOfPost = postModel.textOfPost,
                dateOfPost = dateOfPostString,
                nameAuthor = postModel.user?.username,
                likesCount = likesCount,
                isLikedByUser = isLikedByUser,
                isRepostedByUser = isRepostedByUser,
                repostsCount = repostsCount,
                postType = postModel.postType,
                sourceId = postModel.sourceId,
                address = postModel.address,
                coordinates = postModel.coordinates,
                sourceVideo = postModel.sourceVideo,
                sourceAd = postModel.sourceAd,
                attachmentId = postModel.attachment?.id
            )
        }
    }
}