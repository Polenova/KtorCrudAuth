package polenova

import com.typesafe.config.ConfigFactory.load
import org.apache.commons.codec.Charsets
import org.apache.commons.codec.binary.Hex
import org.springframework.security.crypto.encrypt.Encryptors
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardOpenOption
import java.util.*

fun main() {
    val properties = Properties().apply {
        load(Files.newBufferedReader(Paths.get("./fcm/encrypt.properties")))
    }
    val encryptor = Encryptors.stronger(
        properties.getProperty("password"),
        Hex.encodeHexString(
            properties.getProperty("salt")
                .toByteArray(Charsets.UTF_8)
        )
    )

    val encrypted = encryptor.encrypt(
        Files.readAllBytes(Paths.get("./fcm/fcm-raw.json"))
    )

    Files.write(
        Paths.get("./fcm/fcm-encrypted.json"),
        encrypted,
        StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING
    )
}