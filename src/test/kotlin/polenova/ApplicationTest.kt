package polenova

import com.jayway.jsonpath.JsonPath
import io.ktor.application.Application
import io.ktor.config.MapApplicationConfig
import io.ktor.http.*
import io.ktor.http.content.PartData
import io.ktor.server.testing.*
import io.ktor.util.KtorExperimentalAPI
import kotlinx.io.streams.asInput
import org.junit.Test
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.test.assertEquals
import kotlin.test.assertTrue


class ApplicationTest {
    private val jsonContentType = ContentType.Application.Json.withCharset(Charsets.UTF_8)
    private val multipartBoundary = "***blob***"
    private val multipartContentType =
        ContentType.MultiPart.FormData.withParameter("boundary", multipartBoundary).toString()
    private val uploadPath = "./uploads"

    @KtorExperimentalAPI
    private val configure: Application.() -> Unit = {
        (environment.config as MapApplicationConfig).apply {
            put("polenova.upload.dir", uploadPath)
            put("polenova.fcm.password", "TEST_FCM_PASSWORD")
            put("polenova.fcm.salt", "TEST_FCM_SALT")
            put("polenova.fcm.db-url", "TEST_FCM_DB_URL")
            put("polenova.fcm.path", "./fcm/fcm-encrypted.json")
        }
        module(testing = true)
    }

    @KtorExperimentalAPI
    @Test
    fun testUpload() {
        withTestApplication(configure) {
            val token = with(auth()) {
                JsonPath.read<String>(response.content!!, "$.token")
            }
            with(handleRequest(HttpMethod.Post, "/api/v1/media") {
                addHeader(HttpHeaders.ContentType, multipartContentType)
                addHeader(HttpHeaders.Authorization, "Bearer $token")
                setBody(
                    multipartBoundary,
                    listOf(
                        PartData.FileItem({
                            Files.newInputStream(Paths.get("./src/test/resources/test.png"))
                                .asInput()
                        }, {}, headersOf(
                            HttpHeaders.ContentDisposition to listOf(
                                ContentDisposition.File.withParameter(
                                    ContentDisposition.Parameters.Name,
                                    "file"
                                ).toString(),
                                ContentDisposition.File.withParameter(
                                    ContentDisposition.Parameters.FileName,
                                    "photo.png"
                                ).toString()
                            ),
                            HttpHeaders.ContentType to listOf(ContentType.Image.PNG.toString())
                        )
                        )
                    )
                )
            }) {
                response
                assertEquals(HttpStatusCode.OK, response.status())
                assertTrue(response.content!!.contains("\"id\""))
            }
        }
    }

    @KtorExperimentalAPI
    @Test
    fun testUnauthorized() {
        withTestApplication(configure) {
            with(handleRequest(HttpMethod.Get, "/api/v1/posts")) {
                response
                assertEquals(HttpStatusCode.Unauthorized, response.status())
            }
        }
    }

    @KtorExperimentalAPI
    @Test
    fun testAuth() {
        withTestApplication(configure) {
            with(auth()) {
                assertEquals(HttpStatusCode.OK, response.status())
            }
        }
    }

    private fun TestApplicationEngine.auth(): TestApplicationCall =
        handleRequest(HttpMethod.Post, "/api/v1/authentication") {
            addHeader(HttpHeaders.ContentType, jsonContentType.toString())
            setBody(
                """
                        {
                            "username": "vasya",
                            "password": "password"
                        }
                    """.trimIndent()
            )
        }
}

//fun main() {
//    val properties = Properties().apply {
//        load(Files.newBufferedReader(Paths.get("./fcm/encrypt.properties")))
//    }
//    val encryptor = Encryptors.stronger(
//        properties.getProperty("password"),
//        Hex.encodeHexString(
//            properties.getProperty("salt")
//                .toByteArray(Charsets.UTF_8)
//        )
//    )
//
//    val encrypted = encryptor.encrypt(
//        Files.readAllBytes(Paths.get("./fcm/fcm-raw.json"))
//    )
//
//    Files.write(
//        Paths.get("./fcm/fcm-encrypted.json"),
//        encrypted,
//        StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING
//    )
//}