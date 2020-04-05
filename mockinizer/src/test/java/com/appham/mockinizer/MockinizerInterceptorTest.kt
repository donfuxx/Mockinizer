package com.appham.mockinizer

import com.appham.mockinizer.Method.*
import com.nhaarman.mockitokotlin2.*
import okhttp3.*
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okio.BufferedSink
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class MockinizerInterceptorTest {

    private val chain: Interceptor.Chain = mock()

    private val mockWebServer : MockWebServer = MockWebServer().configure()

    private val systemUnderTest: MockinizerInterceptor = MockinizerInterceptor(mocks, mockWebServer, DummyLogger)

    private val realBaseurl = "https://real.api"

    @BeforeEach
    fun setup() {
        clearInvocations(chain)
    }

    @ParameterizedTest
    @MethodSource("args")
    fun `Should mock response When RequestFilter contains request On intercept`(args: TestData) {

        // build a request for the chain to be intercepted by MockinizerInterceptor
        val requestUrl = "$realBaseurl${args.requestFilter.url()}"
        val request = Request.Builder()
            .url(requestUrl)
            .method(
                args.requestFilter.method.name,
                args.requestFilter.body.orDummyBody(args.requestFilter.method)
            )
            .headers(args.requestFilter.headers ?: Headers.headersOf())
            .build()

        // plugin the request into the MockinizerInterceptor
        whenever(chain.request()).thenReturn(request)
        systemUnderTest.intercept(chain)

        // capture the actual request after MockinizerInterceptor is done intercepting and assert it
        argumentCaptor<Request> {
            verify(chain).proceed(capture())
            val actualRequest = this.firstValue

            // if there is no mock response defined then url original one or localhost otherwise
            if (args.mockResponse == null) {
                assertThat(actualRequest.url).isEqualTo(request.url)
            } else {
                assertThat(actualRequest.url.host).isEqualTo("localhost")
            }

            assertThat(actualRequest.method).isEqualTo(request.method)
            assertThat(actualRequest.headers).isEqualTo(request.headers)
        }
    }

    /**
     * Provides test data for parameterized test
     */
    private fun args() = mutableListOf(

        // Test requests that should NOT get mocked:
        TestData(RequestFilter(), null),
        TestData(RequestFilter(path = "banana"), null),
        TestData(RequestFilter(method = DELETE, body = """{"type":"apple"}"""), null),
        TestData(RequestFilter(method = PATCH, body = """{"type":"apple"}"""), null),
        TestData(RequestFilter(method = PUT, path = "banana", body = """{"type":"apple"}"""), null),
        TestData(RequestFilter(method = POST, path = "banana", body = """{"type":"apple"}"""), null),
        TestData(RequestFilter(method = POST, path = "banana", body = null), null),
        TestData(RequestFilter(method = GET, path = "/typicode/demo/headersNone", headers = Headers.headersOf("a", "b")),
            mockResponse = null),
        TestData(RequestFilter(path = "/typicode/demo/header", headers = Headers.headersOf("a", "b")),
            mockResponse = null),
        TestData(RequestFilter(path = "/typicode/demo/querynomock", query = "param=foo"), null),

        // Test requests that actually should get mocked:
        TestData(RequestFilter(method = POST, path = "/typicode/demo/foo", body = """{"type":"apple"}"""),
            mockResponse = MockResponse().apply {
                setResponseCode(200)
                setBody("""{"title":"I don't care which body you posted!"}""")
            }),
        TestData(RequestFilter(method = POST, path = "/typicode/demo/foo", body = """{"type":"whatever"}"""),
            mockResponse = MockResponse().apply {
                setResponseCode(200)
                setBody("""{"title":"I don't care which body you posted!"}""")
            }),
        TestData(RequestFilter(method = POST, path = "/typicode/demo/foo", body = ""),
            mockResponse = MockResponse().apply {
                setResponseCode(200)
                setBody("""{"title":"I don't care which body you posted!"}""")
            }),
        TestData(RequestFilter(method = POST, path = "/typicode/demo/foo", body = null),
            mockResponse = MockResponse().apply {
                setResponseCode(200)
                setBody("""{"title":"I don't care which body you posted!"}""")
            }),
        TestData(RequestFilter(method = GET, path = "/typicode/demo/headersAny", headers = null),
            mockResponse = MockResponse().apply {
                setResponseCode(200)
                setBody("""{"title":"header is ignored"}""")
            }),
        TestData(RequestFilter(method = GET, path = "/typicode/demo/headersNone", headers = Headers.headersOf()),
            mockResponse = MockResponse().apply {
                setResponseCode(200)
                setBody("""{"title":"only mocked if no headers at all"}""")
            }),
        TestData(RequestFilter(path = "/typicode/demo/query", query = "a=b"),
            mockResponse = MockResponse().apply {
                setResponseCode(503)
            })

        ).apply {

        // All requests from mockinizer mocks map should get mocked:
        addAll(mocks.map { TestData(it.key, it.value) })
    }.stream()

    data class TestData(
        val requestFilter: RequestFilter,
        val mockResponse: MockResponse?
    )

}

/**
 * OkHttp complains if there are POST, PUT or PATCH requests without request body:
 * This returns a dummy request body if needed
 */
private fun String?.orDummyBody(method: Method): RequestBody? =
    this?.toRequestBody()
        ?: when (method) {
            POST,
            PUT,
            PATCH -> object : RequestBody() {
                override fun contentType(): MediaType? {
                    TODO("not implemented")
                }

                override fun writeTo(sink: BufferedSink) {}
            }

            GET,
            DELETE -> null
        }