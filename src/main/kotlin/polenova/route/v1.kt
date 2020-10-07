package polenova

import io.ktor.application.call
import io.ktor.auth.authenticate
import io.ktor.auth.authentication
import io.ktor.features.ParameterConversionException
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.files
import io.ktor.http.content.static
import io.ktor.request.receive
import io.ktor.request.receiveMultipart
import io.ktor.response.respond
import io.ktor.routing.*
import io.ktor.util.KtorExperimentalAPI
import polenova.dto.AuthenticationRequestDto
import polenova.dto.PasswordChangeRequestDto
import polenova.dto.PostRequestDto
import polenova.dto.UserResponseDto
import polenova.model.UserModel
import polenova.service.*

class RoutingV1(
    private val staticPath: String,
    private val postService: PostService,
    private val fileService: FileService,
    private val userService: UserService
) {
    @KtorExperimentalAPI
    fun setup(configuration: Routing) {
        with(configuration) {
            route("/api/v1/") {
                static("/static") {
                    files(staticPath)
                }
                route("/") {
                    post("/registration") {
                        val input = call.receive<AuthenticationRequestDto>()
                        val username = input.username
                        val password = input.password
                        val response = userService.save(username, password)
                        call.respond(response)
                    }
                    post("/authentication") {
                        val input = call.receive<AuthenticationRequestDto>()
                        val response = userService.authenticate(input)
                        call.respond(response)
                    }
                }
                authenticate("basic", "jwt") {
                    route("/me") {
                        get {
                            val me = call.authentication.principal<UserModel>()
                            call.respond(UserResponseDto.fromModel(me!!))
                        }
                        post("/change-password") {
                            val me = call.authentication.principal<UserModel>()
                            val input = call.receive<PasswordChangeRequestDto>()
                            val response = userService.changePassword(me!!.id, input)
                            call.respond(response)
                        }
                    }
                    route("/posts") {
                        get {
                            val me = call.authentication.principal<UserModel>()
                            val response = postService.getAll(me!!.id)
                            call.respond(response)
                        }
                        get("/{id}") {
                            val me = call.authentication.principal<UserModel>()
                            val id = call.parameters["id"]?.toLongOrNull() ?: throw ParameterConversionException(
                                "id",
                                "Long"
                            )
                            val response = postService.getById(id, me!!.id)
                            call.respond(response)
                        }
                        get("/recent") {
                            val me = call.authentication.principal<UserModel>()
                            val response = postService.getRecent(me!!.id)
                            call.respond(response)
                        }
                        get("{id}/get-posts-after") {
                            val me = call.authentication.principal<UserModel>()
                            val id = call.parameters["id"]?.toLongOrNull() ?: throw ParameterConversionException(
                                "id",
                                "Long"
                            )
                            val response = postService.getPostsAfter(id, me!!.id)
                            call.respond(response)
                        }
                        get("{id}/get-posts-before") {
                            val me = call.authentication.principal<UserModel>()
                            val id = call.parameters["id"]?.toLongOrNull() ?: throw ParameterConversionException(
                                "id",
                                "Long"
                            )
                            val response = postService.getPostsBefore(id, me!!.id)
                            call.respond(response)
                        }
                        post {
                            val me = call.authentication.principal<UserModel>()
                            val input = call.receive<PostRequestDto>()
                            postService.save(input, me!!)
                            call.respond(HttpStatusCode.OK)
                        }
                        post("/{id}/like") {
                            val me = call.authentication.principal<UserModel>()
                            val id = call.parameters["id"]?.toLongOrNull() ?: throw ParameterConversionException(
                                "id",
                                "Long"
                            )
                            val response = postService.likeById(id, me!!)
                            call.respond(response)
                        }
                        delete("/{id}/dislike") {
                            val me = call.authentication.principal<UserModel>()
                            val id = call.parameters["id"]?.toLongOrNull() ?: throw ParameterConversionException(
                                "id",
                                "Long"
                            )
                            val response = postService.dislikeById(id, me!!.id)
                            call.respond(response)
                        }
                        post("/{id}/repost") {
                            val me = call.authentication.principal<UserModel>()
                            val id = call.parameters["id"]?.toLongOrNull() ?: throw ParameterConversionException(
                                "id",
                                "Long"
                            )
                            val input = call.receive<PostRequestDto>()
                            val response = postService.repostById(id, me!!, input)
                            call.respond(response)
                        }
                        post {
                            val me = call.authentication.principal<UserModel>()
                            val input = call.receive<PostRequestDto>()
                            postService.save(input, me!!)
                            call.respond(HttpStatusCode.OK)
                        }
                        post("/{id}") {
                            val id = call.parameters["id"]?.toLongOrNull() ?: throw ParameterConversionException(
                                "id",
                                "Long"
                            )
                            val input = call.receive<PostRequestDto>()
                            val me = call.authentication.principal<UserModel>()
                            postService.saveById(id, input, me!!)
                            call.respond(HttpStatusCode.OK)
                        }
                        delete("/{id}") {
                            val me = call.authentication.principal<UserModel>()
                            val id = call.parameters["id"]?.toLongOrNull() ?: throw ParameterConversionException(
                                "id",
                                "Long"
                            )
                            if (!postService.removeById(id, me!!)) {
                                println("You can't delete post of another user")
                            }
                        }
                    }
                }

                route("/media") {
                    post {
                        val multipart = call.receiveMultipart()
                        val response = fileService.save(multipart)
                        call.respond(response)
                    }
                }
            }
        }
    }
}

