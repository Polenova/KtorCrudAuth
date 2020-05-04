import io.ktor.features.ParameterConversionException
import repository.PostRepository
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.ContentNegotiation
import io.ktor.features.NotFoundException
import io.ktor.features.StatusPages
import io.ktor.gson.gson
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.server.cio.EngineMain
import org.kodein.di.generic.bind
import org.kodein.di.generic.singleton
import org.kodein.di.ktor.KodeinFeature
import repository.PostRepositoryInMemoryWithMutexImpl
import route.v1

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.module() {
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
        exception<ParameterConversionException> {e ->
            call.respond(HttpStatusCode.BadRequest)
            throw e
        }
        exception<Throwable> {e ->
            call.respond(HttpStatusCode.InternalServerError)
            throw e
        }
        exception<NotFoundException> {e ->
            call.respond(HttpStatusCode.NotFound)
            throw e
        }
    }

    install(KodeinFeature) {

        bind<PostRepository>() with singleton { PostRepositoryInMemoryWithMutexImpl() }
    }

    install(Routing) {
        v1()
    }
}