package io.github.tomhula.jecnaapi.web

import io.ktor.client.statement.*
import io.ktor.http.*

interface AuthWebClient
{
    /**
     * Logins the client.
     * If **some** user is already logged in, true is returned, no matter if it is the one trying to log in or not.
     * To switch to another login, first call [logout], then [login].
     *
     * @param auth The auth details to login the user with.
     * @return True if login was successful **or someone is already logged in**, false otherwise.
     */
    suspend fun login(auth: Auth): Boolean

    /**
     * Logins the client.
     * If **some** user is already logged in, true is returned, no matter if it is the one trying to log in or not.
     * To switch to another login, first call [logout], then [login].
     *
     * @param username The username to login.
     * @param password The [username's][username] password.
     * @return True if login was successful **or someone is already logged in**, false otherwise.
     * @see [login]
     */
    suspend fun login(username: String, password: String) = login(Auth(username, password))

    /**
     * Logouts the client.
     *
     * @see [login]
     */
    suspend fun logout()

    /**
     * @return Whether this [client][AuthWebClient] is logged in or not.
     */
    suspend fun isLoggedIn(): Boolean

    /**
     * Makes a request to the provided path. Responses may vary depending on whether user is logged in or not.
     *
     * @param path Relative path from the domain. Must include first slash.
     * @param parameters HTTP parameters, which will be sent URL encoded.
     * @throws AuthenticationException When the query fails because user is not authenticated.
     * @return The [HttpResponse].
     */
    suspend fun query(path: String, parameters: Parameters? = null): HttpResponse

    /**
     * Makes a request to the provided path. Responses may vary depending on whether user is logged in or not.
     *
     * @param path Relative path from the domain. Must include first slash.
     * @param parameters HTTP parameters, which will be sent URL encoded.
     * @throws AuthenticationException When the query fails because user is not authenticated.
     * @return The HTTP response's body as [String].
     */
    suspend fun queryStringBody(path: String, parameters: Parameters? = null) = query(path, parameters).bodyAsText()
}

/**
 * [append] extension function on [ParametersBuilder], that takes [Pair] as a parameter.
 */
fun ParametersBuilder.append(pair: Pair<String, Any>) = append(pair.first, pair.second.toString())
