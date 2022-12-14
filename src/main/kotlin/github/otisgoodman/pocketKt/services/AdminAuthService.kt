package github.otisgoodman.pocketKt.services

import github.otisgoodman.pocketKt.Client
import github.otisgoodman.pocketKt.models.Admin
import github.otisgoodman.pocketKt.services.utils.CrudService
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive

//@TODO Document
class AdminAuthService(client: Client) : CrudService<Admin>(client) {

    @Serializable
    data class AdminAuthResponse(val token: String, val admin: Admin, val data: Map<String, JsonElement>)

    override val baseCrudPath = "/api/admins"

    override fun decode(data: Map<String, JsonElement>): Admin {
        return Admin(data)
    }

    suspend fun authWithPassword(
        email: String, password: String, bodyParams: Map<String, JsonElement>, queryParams: Map<String, String>
    ): AdminAuthResponse {
        val params = mapOf(
            *bodyParams.toList().toTypedArray(),
            "identity" to JsonPrimitive(email),
            "password" to JsonPrimitive(password)
        )
        val response = client.httpClient.post {
            url {
                path(baseCrudPath, "auth-with-password")
                contentType(ContentType.Application.Json)
                queryParams.forEach { parameters.append(it.key, it.value) }
                header("Authorization", "")
                setBody(Json.encodeToString(params))
            }
        }.body<AdminAuthResponse>()
        return response
    }

    suspend fun authRefresh(): AdminAuthResponse {
        val response = client.httpClient.post {
            url {
                path(baseCrudPath, "auth-refresh")
            }
        }.body<AdminAuthResponse>()
        return response
    }


    suspend fun requestPasswordReset(
        email: String, body: Map<String, JsonElement>, queryParams: Map<String, String>
    ): Boolean {
        val params = mapOf(
            *body.toList().toTypedArray(), "email" to JsonPrimitive(email)
        )
        return client.httpClient.post {
            url {
                path(baseCrudPath, "request-password-reset")
                contentType(ContentType.Application.Json)
                queryParams.forEach { parameters.append(it.key, it.value) }
                setBody(Json.encodeToString(params))
            }
        }.run { true }
    }

    suspend fun confirmPasswordReset(
        passwordResetToken: String,
        password: String,
        passwordConfirm: String,
        body: Map<String, JsonElement>,
        queryParams: Map<String, String>
    ): UserAuthService.UserAuthResponse {
        val params = mapOf(
            *body.toList().toTypedArray(),
            "token" to JsonPrimitive(passwordResetToken),
            "password" to JsonPrimitive(password),
            "passwordConfirm" to JsonPrimitive(passwordConfirm)
        )
        val response = client.httpClient.post {
            url {
                path(baseCrudPath, "confirm-password-reset")
                contentType(ContentType.Application.Json)
                queryParams.forEach { parameters.append(it.key, it.value) }
                setBody(Json.encodeToString(params))
            }
        }.body<UserAuthService.UserAuthResponse>()
        return response
    }


}