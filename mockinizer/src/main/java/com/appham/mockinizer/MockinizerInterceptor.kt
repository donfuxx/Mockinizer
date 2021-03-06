package com.appham.mockinizer

import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer


class MockinizerInterceptor(
    private val mocks: Map<RequestFilter, MockResponse> = emptyMap(),
    private val mockServer: MockWebServer,
    private val log: Logger = DebugLogger
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {

        fun findMockResponse(request: Request): MockResponse? {
            return with(RequestFilter.from(request, log)) {
                val foundMockResponse = mocks[this]
                    ?: mocks[copy(body = null)]
                    ?: mocks[copy(headers = null)]
                    ?: mocks[copy(query = null)]
                    ?: mocks[copy(body = null, headers = null)]
                    ?: mocks[copy(body = null, query = null)]
                    ?: mocks[copy(headers = null, query = null)]
                    ?: mocks[copy(body = null, headers = null, query = null)]

                if (foundMockResponse == null) {
                    log.d("No mocks found for $request")
                } else {
                    log.d("Found Mock response: $foundMockResponse " +
                            "for request: $request")
                }

                foundMockResponse
            }
        }

        fun Interceptor.Chain.findServer(): HttpUrl =
            when (findMockResponse(request())) {
                is MockResponse -> {
                    request().url.newBuilder()
                        .host(mockServer.hostName)
                        .port(mockServer.port)
//                .scheme("http") //TODO: make http - https configurable
                        .build()
                }
                else -> request().url
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


