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

        assertEquals(expectedBody, actualResponse.body())
        assertEquals(expectedUrl, actualResponse.raw().request.url.toString())
        assertEquals(expectedStatusCode, actualResponse.code())
    }

    @Test
    fun testShouldCallMockServer_WhenMockError500ApiCalled() {
        val actualResponse = TestApiService.testApi.getMockedError500().execute()
        val expectedBody = null
        val expectedUrl = "${mockServerUrl}error500"
        val expectedStatusCode = 500

        assertEquals(expectedBody, actualResponse.body())
        assertEquals(expectedUrl, actualResponse.raw().request.url.toString())
        assertEquals(expectedStatusCode, actualResponse.code())
    }

}
