package polenova

import com.jayway.jsonpath.JsonPath
import io.ktor.application.Application
import io.ktor.config.MapApplicationConfig
import io.ktor.http.*
import io.ktor.http.content.PartData
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import io.ktor.server.testing.withTestApplication
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
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
            put("ncraft.upload.dir", uploadPath)
        }
        module()
    }

    @KtorExperimentalAPI
    @Test
    fun testUpload() {
        withTestApplication(configure) {
            with(handleRequest(HttpMethod.Post, "/api/v1/media") {
                addHeader(HttpHeaders.ContentType, multipartContentType)
                setBody(
                    multipartBoundary,
                    listOf(
                        PartData.FileItem({
                            Files.newInputStream(Paths.get("./src/test/resources/test.png")).asInput()
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
            runBlocking {
                var token: String? = null
                with(handleRequest(HttpMethod.Post, "/api/v1/authentication") {
                    addHeader(HttpHeaders.ContentType, jsonContentType.toString())
                    setBody(
                        """
                        {
                            "username": "vasya",
                            "password": "password"
                        }
                    """.trimIndent()
                    )
                }) {
                    println(response.content)
                    response
                    assertEquals(HttpStatusCode.OK, response.status())
                    token = JsonPath.read<String>(response.content!!, "$.token")
                }
                delay(500)
                with(handleRequest(HttpMethod.Get, "/api/v1/me") {
                    addHeader(HttpHeaders.Authorization, "Bearer $token")
                }) {
                    response
                    assertEquals(HttpStatusCode.OK, response.status())
                    val username = JsonPath.read<String>(response.content!!, "$.username")
                    assertEquals("vasya", username)
                }
            }
        }
    }

    @Test
    fun testExpire() {
        withTestApplication(configure) {
            runBlocking {
                var token: String? = null
                with(handleRequest(HttpMethod.Post, "/api/v1/authentication") {
                    addHeader(HttpHeaders.ContentType, jsonContentType.toString())
                    setBody(
                        """
                        {
                            "username": "vasya",
                            "password": "password"
                        }
                    """.trimIndent()
                    )
                }) {
                    println(response.content)
                    response
                    assertEquals(HttpStatusCode.OK, response.status())
                    token = JsonPath.read<String>(response.content!!, "$.token")
                }
                delay(5000)
                with(handleRequest(HttpMethod.Get, "/api/v1/me") {
                    addHeader(HttpHeaders.Authorization, "Bearer $token")
                }) {
                    response
                    assertEquals(HttpStatusCode.Unauthorized, response.status())
                }
            }
        }
    }
}