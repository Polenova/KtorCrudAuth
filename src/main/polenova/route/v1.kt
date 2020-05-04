package route

import dto.PostRequestDto
import dto.PostResponseDto
import io.ktor.application.call
import io.ktor.features.NotFoundException
import io.ktor.features.ParameterConversionException
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.*
import model.PostModel
import org.kodein.di.generic.instance
import org.kodein.di.ktor.kodein
import repository.PostRepository

fun Routing.v1() {
    route("/api/v1/posts") {
        val repo by kodein().instance<PostRepository>()
        get {
            val response = repo.getAll().map { PostResponseDto.fromModel(it) }
            call.respond(response)
        }
        get("/{id}") {
            val id = call.parameters["id"]?.toLongOrNull() ?: throw ParameterConversionException("id", "Long")
            val model = repo.getById(id) ?: throw NotFoundException()
            val response = PostResponseDto.fromModel(model)
            call.respond(response)
        }
        get("/{id}/like") {
            val id = call.parameters["id"]?.toLongOrNull() ?: throw ParameterConversionException("id", "Long")
            val model = repo.likeById(id) ?: throw NotFoundException()
            val response = PostResponseDto.fromModel(model)
            call.respond(response)
        }
        get("/{id}/comment") {
            val id = call.parameters["id"]?.toLongOrNull() ?: throw ParameterConversionException("id", "Long")
            val model = repo.commentById(id) ?: throw NotFoundException()
            val response = PostResponseDto.fromModel(model)
            call.respond(response)
        }
        get("/{id}/share") {
            val id = call.parameters["id"]?.toLongOrNull() ?: throw ParameterConversionException("id", "Long")
            val model = repo.shareById(id) ?: throw NotFoundException()
            val response = PostResponseDto.fromModel(model)
            call.respond(response)
        }
        post {
            val input = call.receive<PostRequestDto>()
            val model = PostModel(
                id = input.id,
                author = input.author,
                content = input.content,
                created = input.created,
                sourceHTTP = input.sourceHTTP,
                typePost = input.postType
            )
            val response = repo.save(model)
            call.respond(response)
        }
        delete("/{id}") {
            val id = call.parameters["id"]?.toLongOrNull() ?: throw ParameterConversionException("id", "Long")
            repo.removeById(id)
        }
    }
}
