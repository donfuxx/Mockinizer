package com.appham.mockinizer

import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals

internal class MockinizerAndroidTest {

    private val realServerUrl = BuildConfig.BASE_URL
    private val mockServerUrl = "https://localhost:${BuildConfig.MOCKSERVER_PORT}/typicode/demo/"

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

}
