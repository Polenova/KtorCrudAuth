package polenova.dto

import polenova.model.Coordinates
import polenova.model.PostType

class PostRequestDto (
    val textOfPost: String? = null,
    val postType: PostType = PostType.POST,
    val address: String? = null,
    val coordinates: Coordinates? = null,
    val sourceVideo: String? = null,
    val sourceAd: String? = null,
    val attachmentId: String? = null
)