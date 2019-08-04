package com.appham.mockinizer

import com.nhaarman.mockitokotlin2.*
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class MockinizerInterceptorTest {

    private val mocks: Map<RequestFilter, MockResponse> = mapOf(
        RequestFilter("/typicode/demo/mocked") to MockResponse().apply {
            setResponseCode(200)
            setBody("""{"foo":"bar"}""")
        },
        RequestFilter("/typicode/demo/foo") to MockResponse().apply {
            setResponseCode(200)
        },
        RequestFilter("/typicode/demo/error500") to MockResponse().apply {
            setResponseCode(500)
        },
        RequestFilter("/typicode/demo/private") to MockResponse().apply {
            setResponseCode(403)
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
    fun `Should mock response When RequestFilter contains request url On intercept`(args: TestData) {
        val requestUrl = "$realBaseurl${args.requestFilter.path.orEmpty()}"
        val originalRequest = Request.Builder().url(requestUrl).build()

        whenever(chain.request()).thenReturn(originalRequest)
        systemUnderTest.intercept(chain)

        argumentCaptor<Request> {
            verify(chain).proceed(capture())
            if (args.mockResponse != null) {
                assertNotEquals(originalRequest.url, this.firstValue.url)
            } else {
                assertEquals(originalRequest.url, this.firstValue.url)
            }
        }
    }

    private fun args() = mutableListOf(
        TestData(RequestFilter(), MockResponse()),
        TestData(RequestFilter(path = "banana"), null),
        TestData(RequestFilter(body = """{"type":"apple"}"""), null),
        TestData(RequestFilter(path = "banana", body = """{"type":"apple"}"""), null)
    ).apply {
        addAll(mocks.map { TestData(it.key, it.value) })
    }.stream()

    data class TestData(
        val requestFilter: RequestFilter,
        val mockResponse: MockResponse?
    )

}
