package com.appham.mockinizer

import okhttp3.Request
import okhttp3.RequestBody
import okio.Buffer

data class RequestFilter(
    val path: String? = null,
    val method: Method = Method.GET,
    val body: String? = null
) {

    companion object {

        fun from(request: Request) =
            RequestFilter(
                path = request.url.encodedPath,
                method = getMethodOrDefault(request.method),
                body = request.body?.asString()
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
