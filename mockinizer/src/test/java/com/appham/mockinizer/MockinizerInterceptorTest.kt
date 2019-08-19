package com.appham.mockinizer

import com.nhaarman.mockitokotlin2.*
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.mockwebserver.MockResponse
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class MockinizerInterceptorTest {

    private val chain: Interceptor.Chain = mock()

    private val systemUnderTest: MockinizerInterceptor = MockinizerInterceptor(mocks)

    private val realBaseurl = "https://foo.bar"

    @BeforeEach
    fun setup() {
        clearInvocations(chain)
    }

    @ParameterizedTest
    @MethodSource("args")
    fun `Should mock response When RequestFilter contains request On intercept`(args: TestData) {

        // build a request for the chain to be intercepted by MockinizerInterceptor
        val requestUrl = "$realBaseurl${args.requestFilter.path.orEmpty()}"
        val request = Request.Builder()
            .url(requestUrl)
            .method(args.requestFilter.method.name, args.requestFilter.body?.toRequestBody())
            .headers(args.requestFilter.headers)
            .build()

        // plugin the request into the MockinizerInterceptor
        whenever(chain.request()).thenReturn(request)
        systemUnderTest.intercept(chain)

        // capture the actual request after MockinizerInterceptor is done intercepting and assert it
        argumentCaptor<Request> {
            verify(chain).proceed(capture())
            val actualRequest = this.firstValue

            // if there is no mock response defined then url original one or not otherwise
            if (args.mockResponse == null) {
                assertThat(actualRequest.url).isEqualTo(request.url)
            } else {
                assertThat(actualRequest.url).isNotEqualTo(request.url)
            }

            assertThat(actualRequest.method).isEqualTo(request.method)
            assertThat(actualRequest.headers).isEqualTo(request.headers)
        }
    }

    private fun args() = mutableListOf(
        TestData(RequestFilter(), null),
        TestData(RequestFilter(path = "banana"), null),
        TestData(RequestFilter(method = Method.DELETE, body = """{"type":"apple"}"""), null),
        TestData(RequestFilter(method = Method.PATCH, body = """{"type":"apple"}"""), null),
        TestData(RequestFilter(method = Method.PUT, path = "banana", body = """{"type":"apple"}"""), null),
        TestData(RequestFilter(method = Method.POST, path = "banana", body = """{"type":"apple"}"""), null)
    ).apply {
        addAll(mocks.map { TestData(it.key, it.value) })
    }.stream()

    data class TestData(
        val requestFilter: RequestFilter,
        val mockResponse: MockResponse?
    )

}
