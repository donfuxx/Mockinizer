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

    private val realBaseurl = "https://foo.bar/"

    @BeforeEach
    fun setup() {
        clearInvocations(chain)
    }

    @ParameterizedTest
    @MethodSource("arguments")
    fun `Should mock response When RequestFilter contains request url On intercept`(arguments: TestArguments) {

        val originalRequest = Request.Builder().url(arguments.requestUrl).build()
        whenever(chain.request()).thenReturn(originalRequest)
        systemUnderTest.intercept(chain)

        argumentCaptor<Request> {
            verify(chain).proceed(capture())
            if (arguments.isMocked) {
                assertNotEquals(originalRequest.url, this.firstValue.url)
            } else {
                assertEquals(originalRequest.url, this.firstValue.url)
            }
        }
    }

    private fun arguments() = listOf(
                TestArguments("${realBaseurl}typicode/demo/mocked", true),
                TestArguments("${realBaseurl}typicode/demo/foo", true),
                TestArguments("${realBaseurl}foo", false),
                TestArguments("${realBaseurl}meh", false)
            ).stream()

    data class TestArguments(
        val requestUrl: String,
        val isMocked: Boolean
    )
}
