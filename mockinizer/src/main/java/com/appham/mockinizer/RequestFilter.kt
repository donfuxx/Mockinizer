package com.appham.mockinizer

import okhttp3.Headers
import okhttp3.Request
import okhttp3.RequestBody
import okio.Buffer

/**
 * This class is to define the requests that should get filtered and served by the mock server.
 * @param path the path part of the request url. The default is null
 * @param method the method of the request. The default is GET
 * @param body the request body. Cannot be used together with GET requests. The default is null
 * @param headers the http headers to filter. The default is empty headers
 */
data class RequestFilter(
    val path: String? = null,
    val method: Method = Method.GET,
    val body: String? = null,
    val headers: Headers = Headers.headersOf()
) {

    companion object {

        fun from(request: Request) =
            RequestFilter(
                path = request.url.encodedPath,
                method = getMethodOrDefault(request.method),
                body = request.body?.asString(),
                headers = request.headers
            )

        private fun getMethodOrDefault(method:String) =
            try {
                Method.valueOf(method)
            } catch (e:IllegalArgumentException) {
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
