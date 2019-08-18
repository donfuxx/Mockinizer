package com.appham.mockinizer

import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals

internal class MockinizerAndroidTest {

    @Test
    fun testShouldCallRealServer_WhenPostsApiCalled() {
        val actualResponse = TestApiService.testApi.getPosts().execute()
        val expectedBody = listOf(Post(1, "Post 1"), Post(2, "Post 2"), Post(3, "Post 3"))
        val expectedUrl = "${BuildConfig.BASE_URL}posts"
        val expectedStatusCode = 200

        assertEquals(expectedBody, actualResponse.body())
        assertEquals(expectedUrl, actualResponse.raw().request.url.toString())
        assertEquals(expectedStatusCode, actualResponse.code())

    }

}
