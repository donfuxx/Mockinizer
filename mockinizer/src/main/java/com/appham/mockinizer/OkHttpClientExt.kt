package com.appham.mockinizer

import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse

fun OkHttpClient.Builder.mockinize(mocks: Map<RequestFilter, MockResponse>): OkHttpClient.Builder {
    this.addInterceptor(MockinizerInterceptor(mocks))
    return this
}