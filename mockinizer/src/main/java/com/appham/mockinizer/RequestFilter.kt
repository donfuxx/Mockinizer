package com.appham.mockinizer

import okhttp3.Headers
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.mockwebserver.RecordedRequest
import okio.Buffer

/**
 * This class is to define the requests that should get filtered and served by the mock server.
 * Setting a parameter to null means that any values for that parameter should be filtered.
 * @param path the path part of the request url. The default is null
 * @param method the method of the request. The default is GET
 * @param body the request body. Cannot be used together with GET requests. The default is null
 * @param headers the http headers to filter. The default is null headers
 */
data class RequestFilter(
    val path: String? = null,
    val method: Method = Method.GET,
    val body: String? = null,
    val headers: Headers? = null
) {

    companion object {

        fun from(request: Request, log: Logger = DebugLogger) =
            RequestFilter(
                path = request.url.encodedPath,
                method = getMethodOrDefault(request.method),
                body = request.body?.asString(),
                headers = request.headers
            ).also {
                log.d(
                    "Created RequestFilter $it \n" +
                            " for request: $request"
                )
            }

        fun from(request: RecordedRequest, log: Logger = DebugLogger) =
            RequestFilter(
                path = request.path,
                method = getMethodOrDefault(request.method),
                body = request.body.clone().readUtf8(),
                headers = request.headers
            ).also {
                log.d(
                    "Created RequestFilter $it \n" +
                            " for recorded request: $request"
                )
            }

        private fun getMethodOrDefault(method: String?) =
            try {
                Method.valueOf(method.orEmpty())
            } catch (e: IllegalArgumentException) {
                Method.GET
            }
    }
}

enum class Method {
    GET, POST, PUT, PATCH, DELETE;
}

fun RequestBody.asString(): String {
    val buffer = Buffer()
    writeTo(buffer)
    return buffer.readUtf8()
}

fun RecordedRequest.asString(): String {
    return "$path  - $method - ${body.clone().readUtf8()} - $headers"
}
