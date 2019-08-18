package com.appham.mockinizer

import com.nhaarman.mockitokotlin2.*
import okhttp3.Headers
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

    private val mocks: Map<RequestFilter, MockResponse> = mapOf(
        RequestFilter(path = "/typicode/demo/mocked") to MockResponse().apply {
            setResponseCode(200)
        },
        RequestFilter(path = "/typicode/demo/foo") to MockResponse().apply {
            setResponseCode(200)
        },
        RequestFilter(path = "/typicode/demo/error500") to MockResponse().apply {
            setResponseCode(500)
        },
        RequestFilter(path = "/typicode/demo/private") to MockResponse().apply {
            setResponseCode(403)
        },
        RequestFilter(path = "/typicode/demo/delete", method = Method.DELETE ) to MockResponse().apply {
            setResponseCode(200)
        },
        RequestFilter(path = "/typicode/demo/post", method = Method.POST, body = """{"hey":"ya"}""" ) to MockResponse().apply {
            setResponseCode(200)
            setBody("""{"foo":"bar"}""")
        },
        RequestFilter(path = "/typicode/demo/header", headers = Headers.headersOf("name", "value")) to MockResponse().apply {
            setResponseCode(200)
        },
        RequestFilter(path = "/typicode/demo/headers", headers = Headers.headersOf("name", "value", "foo", "bar")) to MockResponse().apply {
            setResponseCode(200)
        }
    )

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
