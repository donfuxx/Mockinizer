package com.appham.mockinizer

import okhttp3.mockwebserver.MockResponse

val mocks: Map<RequestFilter, MockResponse> = mapOf(
    RequestFilter(path = "/typicode/demo/mocked") to MockResponse().apply {
        setResponseCode(200)
    }
)