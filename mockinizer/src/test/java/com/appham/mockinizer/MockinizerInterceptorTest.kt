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
        val requestUrl = "$realBaseurl${args.requestFilter.path.orEmpty()}"
        val request = Request.Builder()
            .method(args.requestFilter.method.name, args.requestFilter.body?.toRequestBody())
            .headers(args.requestFilter.headers)
            .url(requestUrl)
            .build()

        whenever(chain.request()).thenReturn(request)
        systemUnderTest.intercept(chain)

        argumentCaptor<Request> {
            verify(chain).proceed(capture())
            if (args.mockResponse == null) {
                assertThat(this.firstValue.url).isEqualTo(request.url)
            } else {
                assertThat(this.firstValue.url).isNotEqualTo(request.url)
            }
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
