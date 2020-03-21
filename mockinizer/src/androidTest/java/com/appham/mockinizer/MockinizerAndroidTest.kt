package com.appham.mockinizer

import org.junit.AfterClass
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals

internal class MockinizerAndroidTest {

    @Test
    fun testShouldCallRealServer_WhenPostsApiCalled() {
        val actualResponse = TestApiService.testApi.getPosts().execute()
        val expectedBody = listOf(Post(1, "Post 1"), Post(2, "Post 2"), Post(3, "Post 3"))
        val expectedUrl = "${realServerUrl}posts"
        val expectedStatusCode = 200

        assertEquals(expectedUrl, actualResponse.raw().request.url.toString())
        assertEquals(expectedBody, actualResponse.body())
        assertEquals(expectedStatusCode, actualResponse.code())
    }

    @Test
    fun testShouldCallMockServer_WhenMockError500ApiCalled() {
        val actualResponse = TestApiService.testApi.getMockedError500().execute()
        val expectedBody = null
        val expectedUrl = "${mockServerUrl}error500"
        val expectedStatusCode = 500

        assertEquals(expectedUrl, actualResponse.raw().request.url.toString())
        assertEquals(expectedBody, actualResponse.body())
        assertEquals(expectedStatusCode, actualResponse.code())
    }

    @Test
    fun testShouldCallMockServer_WhenMockDeleteApiCalled() {
        val actualResponse = TestApiService.testApi.getMockedDelete().execute()
        val expectedBody = Unit
        val expectedUrl = "${mockServerUrl}delete"
        val expectedStatusCode = 200
        val expectedMethod = Method.DELETE.name

        assertEquals(expectedUrl, actualResponse.raw().request.url.toString())
        assertEquals(expectedBody, actualResponse.body())
        assertEquals(expectedStatusCode, actualResponse.code())
        assertEquals(expectedMethod, actualResponse.raw().request.method)
    }

    @Test
    fun testShouldCallMockServer_WhenMockPostApiCalled() {
        val actualResponse = TestApiService.testApi.getMockedPost(Post(title = "hey ya")).execute()
        val expectedBody = Post(title = "foobar")
        val expectedUrl = "${mockServerUrl}post"
        val expectedStatusCode = 200
        val expectedMethod = Method.POST.name

        assertEquals(expectedUrl, actualResponse.raw().request.url.toString())
        assertEquals(expectedBody, actualResponse.body())
        assertEquals(expectedStatusCode, actualResponse.code())
        assertEquals(expectedMethod, actualResponse.raw().request.method)
    }

    @Test
    fun testShouldCallMockServer_WhenMockHeadersApiCalled() {
        val actualResponse = TestApiService.testApi.getMockedHeaders().execute()
        val expectedBody = Unit
        val expectedUrl = "${mockServerUrl}headers"
        val expectedStatusCode = 200
        val expectedMethod = Method.GET.name

        assertEquals(expectedUrl, actualResponse.raw().request.url.toString())
        assertEquals(expectedBody, actualResponse.body())
        assertEquals(expectedStatusCode, actualResponse.code())
        assertEquals(expectedMethod, actualResponse.raw().request.method)
    }

    @Test
    fun testShouldCallRealServer_WhenPartialHeadersApiCalled() {
        val actualResponse = TestApiService.testApi.getMockedHeadersPartial().execute()
        val expectedBody = null
        val expectedUrl = "${realServerUrl}headersPartial"
        val expectedStatusCode = 404
        val expectedMethod = Method.GET.name

        assertEquals(expectedUrl, actualResponse.raw().request.url.toString())
        assertEquals(expectedBody, actualResponse.body())
        assertEquals(expectedStatusCode, actualResponse.code())
        assertEquals(expectedMethod, actualResponse.raw().request.method)
    }

    @Test
    fun testShouldCallRealServer_WhenTooManyHeadersApiCalled() {
        val actualResponse = TestApiService.testApi.getMockedHeadersTooMany().execute()
        val expectedBody = null
        val expectedUrl = "${realServerUrl}headersTooMany"
        val expectedStatusCode = 404
        val expectedMethod = Method.GET.name

        assertEquals(expectedUrl, actualResponse.raw().request.url.toString())
        assertEquals(expectedBody, actualResponse.body())
        assertEquals(expectedStatusCode, actualResponse.code())
        assertEquals(expectedMethod, actualResponse.raw().request.method)
    }

    @Test
    fun testShouldCallMockServer_WhenMockHeadersAnyApiCalled() {
        val actualResponse = TestApiService.testApi.getMockedHeadersAny().execute()
        val expectedBody = Post(title = "header is ignored")
        val expectedUrl = "${mockServerUrl}headersAny"
        val expectedStatusCode = 200
        val expectedMethod = Method.GET.name

        assertEquals(expectedUrl, actualResponse.raw().request.url.toString())
        assertEquals(expectedBody, actualResponse.body())
        assertEquals(expectedStatusCode, actualResponse.code())
        assertEquals(expectedMethod, actualResponse.raw().request.method)
    }

    @Test
    fun testShouldCallMockServer_WhenMockHeadersAnyApi2Called() {
        val actualResponse = TestApiService.testApi.getMockedHeadersAny2().execute()
        val expectedBody = Post(title = "header is ignored")
        val expectedUrl = "${mockServerUrl}headersAny"
        val expectedStatusCode = 200
        val expectedMethod = Method.GET.name

        assertEquals(expectedUrl, actualResponse.raw().request.url.toString())
        assertEquals(expectedBody, actualResponse.body())
        assertEquals(expectedStatusCode, actualResponse.code())
        assertEquals(expectedMethod, actualResponse.raw().request.method)
    }

    @Test
    fun testShouldContainMockinizerHeaders_WhenMockApiCalled() {
        val actualResponse = TestApiService.testApi.getMockedHeadersAny2().execute()

        assertEquals("server" to mockVersionHeader,
            actualResponse.headers().last())
        assertEquals("<-- Real request https://my-json-server.typicode.com/typicode/demo/headersAny is now mocked to HTTP/1.1 200 OK",
            actualResponse.headers()["Mockinizer"]
        )
    }

    @Test
    fun testShouldNotContainMockinizerHeaders_WhenRealApiCalled() {
        val actualResponse = TestApiService.testApi.getPosts().execute()

        assertEquals(0, actualResponse.headers().count { (name, value) ->
            value == mockVersionHeader
        })
    }

    @Test
    fun testShouldNotContainMockinizerHeadersDuplicates_WhenMultipleApiCalls() {
        TestApiService.testApi.getMockedHeaders().execute()
        val actualResponse = TestApiService.testApi.getMockedHeaders().execute()

        assertEquals(1, actualResponse.headers().count { (name, value) ->
            value == mockVersionHeader
        })
    }

    companion object {

        private const val realServerUrl = "https://my-json-server.typicode.com/typicode/demo/"
        private const val mockServerUrl = "https://localhost:34567/typicode/demo/"
        private const val mockVersionHeader = "Mockinizer ${BuildConfig.VERSION_NAME} by Thomas Fuchs-Martin"

        @AfterClass
        fun tearDown() {
            Mockinizer.shutDown()
        }
    }

}
