package polenova.service

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import polenova.model.UserModel

class JWTTokenService {
    private val secret = "5c2dbef6-289c-46e6-8cfd-d8b3292d373a"
    private val algo = Algorithm.HMAC256(secret)

    val verifier: JWTVerifier = JWT.require(algo).build()

    fun generate(model: UserModel): String = JWT.create()
        .withClaim("id", model.id)
        .withClaim("password", model.password)
        .sign(algo)
}