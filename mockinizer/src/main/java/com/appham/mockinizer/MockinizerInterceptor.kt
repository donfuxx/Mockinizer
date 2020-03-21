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
                    ?: mocks[this.copy(body = null)]
                    ?: mocks[this.copy(headers = null)]
                    ?: mocks[this.copy(body = null, headers = null)]

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
            when (val mockResponse = findMockResponse(request())?.clone()) {
                is MockResponse -> {
                    mockResponse.addHeader(
                        "Mockinizer",
                        " <-- Real request ${request().url} is now mocked to $mockResponse"
                    )
                    mockResponse.addHeader(
                        "server",
                        "Mockinizer ${BuildConfig.VERSION_NAME} by Thomas Fuchs-Martin"
                    )
                    mockServer.enqueue(mockResponse)
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


