package com.appham.mockinizer

import android.util.Log
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer


class MockinizerInterceptor(
    private val mocks: Map<RequestFilter, MockResponse> = emptyMap()
) : Interceptor {

    private val mockServer = MockWebServer().configure()

    override fun intercept(chain: Interceptor.Chain): Response {

        fun isMocked(request: Request) =
            null != mocks[RequestFilter.from(request)]?.also {
                mockServer.enqueue(it)
            }

        fun Interceptor.Chain.findServer(): HttpUrl = when(isMocked(request())) {
            true -> request().url.newBuilder()
                .host(mockServer.hostName)
                .port(mockServer.port)
//                .scheme("http") //TODO: make http - https configurable
                .build()
                .also {
                    Log.w(javaClass.simpleName, "--> url: ${request().url} mockinized to: $it")
                }
            false -> request().url
        }

        return with(chain) {
            proceed(
                request().newBuilder()
                    .url(findServer())
                    .build()
            )
        }
    }

}


