package dto

import model.PostModel
import model.TypePost

class PostRequestDto (
    val id: Long,
    val content: String? = null,
    val author: String,
    val created: String,
    val postType: TypePost = TypePost.POST,
    val source: PostModel? = null,
    val sourceHTTP: String? = null
) {

}