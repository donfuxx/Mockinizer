package com.appham.mockinizer

import okhttp3.Request

data class RequestFilter(
    val path: String? = null,
    val body: String? = null
) {

    companion object {

        fun from(request: Request) =
            RequestFilter(
                path = request.url.encodedPath,
                body = request.body?.toString()
            )
    }
}