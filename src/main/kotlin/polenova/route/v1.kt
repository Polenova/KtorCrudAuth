
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
import polenova.dto.TokenDto
import polenova.model.UserModel
import polenova.service.FCMService
import polenova.service.FileService
import polenova.service.PostService
import polenova.service.UserService

class RoutingV1(
    private val staticPath: String,
    private val postService: PostService,
    private val fileService: FileService,
    private val userService: UserService,
    private val fcmService: FCMService
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

                authenticate {
                    route("/") {
                        post("/change-password"){
                            val me = call.authentication.principal<UserModel>()
                            val input = call.receive<PasswordChangeRequestDto>()
                            val response = userService.changePassword(me!!.id, input)
                            call.respond(response)
                        }
                    }

                    route("/firebase-token") {
                        post {
                            val me = call.authentication.principal<UserModel>()
                            val token = call.receive<TokenDto>()
                            userService.saveFirebaseToken(me!!.id, token.token)
                            call.respond(HttpStatusCode.OK)
                            fcmService.send(me!!.id, token.token, "hello")
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
                        get("/lastContent") {
                            val me = call.authentication.principal<UserModel>()
                            val response = postService.getLastContent(me!!.id)
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
                            val response = postService.likeById(id, me!!.id)
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
                        post("/{id}") {
                            val me = call.authentication.principal<UserModel>()!!
                            val id = call.parameters["id"]?.toLongOrNull() ?: throw ParameterConversionException(
                                "id",
                                "Long"
                            )
                            val input = call.receive<PostRequestDto>()
                            postService.saveById(id, input, me!!)
                            call.respond(HttpStatusCode.OK)
                        }
                        delete("/{id}/post") {
                            val me = call.authentication.principal<UserModel>()!!
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