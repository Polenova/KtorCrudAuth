package polenova.service

import io.ktor.features.NotFoundException
import io.ktor.util.KtorExperimentalAPI
import org.springframework.security.crypto.password.PasswordEncoder
import polenova.dto.AuthenticationRequestDto
import polenova.dto.AuthenticationResponseDto
import polenova.dto.PasswordChangeRequestDto
import polenova.dto.UserResponseDto
import polenova.exception.InvalidPasswordException
import polenova.exception.NullUsernameOrPasswordException
import polenova.exception.PasswordChangeException
import polenova.exception.UserExistsException
import polenova.model.UserModel
import polenova.repository.UserRepository

class UserService(
    private val repo: UserRepository,
    private val tokenService: JWTTokenService,
    private val passwordEncoder: PasswordEncoder
) {

    suspend fun getModelByIdPassword(id: Long, password: String): UserModel? {
        return repo.getByIdPassword(id, password)
    }

    suspend fun getByUserName(username: String): UserModel? {
        return repo.getByUsername(username)
    }

    @KtorExperimentalAPI
    suspend fun getById(id: Long): UserResponseDto {
        val model = repo.getById(id) ?: throw NotFoundException()
        return UserResponseDto.fromModel(model)
    }

    @KtorExperimentalAPI
    suspend fun changePassword(id: Long, input: PasswordChangeRequestDto): AuthenticationResponseDto {
        val model = repo.getById(id) ?: throw NotFoundException()
        if (!passwordEncoder.matches(input.old, model.password)) {
            throw PasswordChangeException("Wrong password!")
        }
        val copy = model.copy(password = passwordEncoder.encode(input.new))
        repo.save(copy)
        val token = tokenService.generate(copy)
        return AuthenticationResponseDto(token)
    }

    @KtorExperimentalAPI
    suspend fun authenticate(input: AuthenticationRequestDto): AuthenticationResponseDto {
        val model = repo.getByUsername(input.username) ?: throw NotFoundException()
        if (!passwordEncoder.matches(input.password, model.password)) {
            throw InvalidPasswordException("Wrong password!")
        }

        val token = tokenService.generate(model)
        return AuthenticationResponseDto(token)
    }

    suspend fun save(username: String, password: String): AuthenticationResponseDto {
        if (username == "" || password == "") {
            throw NullUsernameOrPasswordException("Username or password is empty")
        } else if (repo.getByUsername(username) != null) {
            throw UserExistsException("User already exists")
        } else {
            val model = repo.save(UserModel(username = username, password = passwordEncoder.encode(password)))
            val token = tokenService.generate(model)
            return AuthenticationResponseDto(token)
        }
    }
}