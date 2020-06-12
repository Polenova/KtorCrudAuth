
import io.ktor.application.call
import io.ktor.auth.authenticate
import io.ktor.auth.authentication
import io.ktor.features.ParameterConversionException
import io.ktor.http.content.files
import io.ktor.http.content.static
import io.ktor.request.receive
import io.ktor.request.receiveMultipart
import io.ktor.response.respond
import io.ktor.routing.*
import polenova.dto.AuthenticationRequestDto
import polenova.dto.PasswordChangeRequestDto
import polenova.dto.PostRequestDto
import polenova.dto.UserResponseDto
import polenova.model.UserModel
import polenova.service.FileService
import polenova.service.PostService
import polenova.service.UserService

class RoutingV1(
    private val staticPath: String,
    private val postService: PostService,
    private val fileService: FileService,
    private val userService: UserService
) {
    fun setup(configuration: Routing) {
        with(configuration) {
            route("/api/v1/") {
                static("/static") {
                    files(staticPath)
                }

                route("/") {
                    post("/registration") {
                        // TODO()
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

                authenticate {
                    route("/me") {
                        get {
                            val me = call.authentication.principal<UserModel>()
                            call.respond(UserResponseDto.fromModel(me!!))
                        }
                        post("/change-password"){
                            val me = call.authentication.principal<UserModel>()
                            val input = call.receive<PasswordChangeRequestDto>()
                            val response = userService.changePassword(me!!.id, input)
                            call.respond(response)
                        }
                    }

                    route("/posts") {
                        get {
                            val response = postService.getAll()
                            call.respond(response)
                        }
                        get("/{id}") {
                            val id = call.parameters["id"]?.toLongOrNull() ?: throw ParameterConversionException(
                                "id",
                                "Long"
                            )
                            val response = postService.getById(id)
                            call.respond(response)
                        }
                        post {
                            val input = call.receive<PostRequestDto>()
                            val response = postService.save(input)
                            call.respond(response)
                        }
                        post("/{id}/like") {
                            val id = call.parameters["id"]?.toLongOrNull() ?: throw ParameterConversionException(
                                "id",
                                "Long"
                            )
                            val response = postService.likeById(id)
                            call.respond(response)
                        }
                        post("/{id}/share") {
                            val me = call.authentication.principal<UserModel>()!!
                            val id = call.parameters["id"]?.toLongOrNull() ?: throw ParameterConversionException(
                                "id",
                                "Long"
                            )
                            val input = call.receive<PostRequestDto>()
                            val response = postService.shareById(id, me, input)
                            call.respond(response)
                        }
                        delete("/{id}/post") {
                            val me = call.authentication.principal<UserModel>()!!
                            // в me - информация о текущем пользователе
                            //TODO()
                            val id = call.parameters["id"]?.toLongOrNull() ?: throw ParameterConversionException(
                                "id",
                                "Long"
                            )
                            if (!postService.removeById(id, me)) println("You can't delete post of another user")
                            //val response = postService.removeById(id, me!!)
                            //call.respond(response)
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
}