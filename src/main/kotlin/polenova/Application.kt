package polenova

import io.ktor.application.*
import io.ktor.auth.Authentication
import io.ktor.auth.UserPasswordCredential
import io.ktor.auth.basic
import io.ktor.auth.jwt.jwt
import io.ktor.response.*
import io.ktor.request.*
import io.ktor.routing.*
import io.ktor.http.*
import io.ktor.gson.*
import io.ktor.features.*
import io.ktor.server.cio.EngineMain
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.runBlocking
import org.bouncycastle.openssl.PasswordException
import org.kodein.di.generic.*
import org.kodein.di.ktor.KodeinFeature
import org.kodein.di.ktor.kodein
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import polenova.exception.*
import polenova.repository.*
import polenova.service.*
import polenova.repository.PostRepository
import polenova.repository.PostRepositoryInMemoryWithMutexImpl
import polenova.repository.UserRepository
import polenova.repository.UserRepositoryInMemoryWithAtomicImpl
import javax.naming.ConfigurationException

fun main(args: Array<String>) {
    EngineMain.main(args)
}

@KtorExperimentalAPI
fun Application.module(testing: Boolean = false) {
    install(ContentNegotiation) {
        gson {
            setPrettyPrinting()
            serializeNulls()
        }
    }
    install(StatusPages) {
        exception<NotImplementedError> { e ->
            call.respond(HttpStatusCode.NotImplemented)
            throw e
        }
        exception<ParameterConversionException> { e ->
            call.respond(HttpStatusCode.BadRequest)
            throw e
        }
        exception<Throwable> { e ->
            call.respond(HttpStatusCode.InternalServerError)
            throw e
        }
        exception<NotFoundException> { e ->
            call.respond(HttpStatusCode.NotFound)
            throw e
        }
        exception<UserExistsException> { error ->
            call.respond(HttpStatusCode.BadRequest)
            throw error
        }
        exception<UserAccessException> { error ->
            call.respond(HttpStatusCode.Forbidden)
            throw error
        }
        exception<PasswordChangeException> { error ->
            call.respond(HttpStatusCode.Forbidden)
            throw error
        }
        exception<InvalidPasswordException> { error ->
            call.respond(HttpStatusCode.Unauthorized)
            throw error
        }
        exception<NullUsernameOrPasswordException> { error ->
            call.respond(HttpStatusCode.BadRequest)
            throw error
        }
        exception<ConfigurationException> { error ->
            call.respond(HttpStatusCode.NotFound)
            throw error
        }
    }
    install(KodeinFeature) {
        constant(tag = "upload-dir") with (environment.config.propertyOrNull("polenova.upload.dir")?.getString()
            ?: throw ConfigurationException("Upload dir is not specified"))
        bind<PasswordEncoder>() with eagerSingleton { BCryptPasswordEncoder() }
        bind<JWTTokenService>() with eagerSingleton { JWTTokenService() }
        bind<PostRepository>() with eagerSingleton { PostRepositoryInMemoryWithMutexImpl() }
        bind<PostService>() with eagerSingleton { PostService(instance()) }
        bind<FileService>() with eagerSingleton { FileService(instance(tag = "upload-dir")) }
        bind<UserRepository>() with eagerSingleton { UserRepositoryInMemoryWithAtomicImpl() }
        bind<UserService>() with eagerSingleton { UserService(instance(), instance(), instance()) }
        constant(tag = "fcm-password") with (environment.config.propertyOrNull("polenova.fcm.password")?.getString()
            ?: throw ConfigurationException("FCM Password is not specified"))
        constant(tag = "fcm-salt") with (environment.config.propertyOrNull("polenova.fcm.salt")?.getString()
            ?: throw ConfigurationException("FCM Salt is not specified"))
        constant(tag = "fcm-db-url") with (environment.config.propertyOrNull("polenova.fcm.db-url")?.getString()
            ?: throw ConfigurationException("FCM DB Url is not specified"))
        constant(tag = "fcm-path") with (environment.config.propertyOrNull("polenova.fcm.path")?.getString()
            ?: throw ConfigurationException("FCM JSON Path is not specified"))

        bind<FCMService>() with eagerSingleton {
            FCMService(
                instance(tag = "fcm-db-url"),
                instance(tag = "fcm-password"),
                instance(tag = "fcm-salt"),
                instance(tag = "fcm-path")
            )
        }

        bind<RoutingV1>() with eagerSingleton {
            RoutingV1(
                instance(tag = "upload-dir"),
                instance(),
                instance(),
                instance()
            )
        }
    }

    install(Authentication) {
        jwt ("jwt") {
            val jwtService by kodein().instance<JWTTokenService>()
            verifier(jwtService.verifier)
            val userService by kodein().instance<UserService>()
            validate {
                val id = it.payload.getClaim("id").asLong()
                val password = it.payload.getClaim("password").asString()
                userService.getModelByIdPassword(id, password)
            }
        }
        basic("basic") {
            val encoder by kodein().instance<PasswordEncoder>()
            val userService by kodein().instance<UserService>()
            validate { credentials ->
                val user = userService.getByUserName(credentials.name)
                if (encoder.matches(credentials.password, user?.password)) {
                    user
                } else {
                    null
                }
            }
        }
    }
    install(Routing) {
        val routingV1 by kodein().instance<RoutingV1>()
        routingV1.setup(this)
    }
}


