package com.appham.mockinizer

import android.util.Log
import io.reactivex.Completable
import io.reactivex.rxkotlin.Observables
import io.reactivex.schedulers.Schedulers
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer

class MockinizerInterceptor(
    private val mocks: Map<RequestFilter, MockResponse> = emptyMap()
) : Interceptor {

    private val mockServer = MockWebServer().apply {
        Completable.fromAction { start() }
            .onErrorComplete()
            .subscribeOn(Schedulers.io())
            .subscribe()
    }

    override fun intercept(chain: Interceptor.Chain): Response {

        fun isMocked(request: Request) =
            null != mocks[RequestFilter.from(request)]?.also {
                mockServer.enqueue(it)
            }

        fun Interceptor.Chain.findServer(): HttpUrl = when(isMocked(request())) {
            true -> request().url.newBuilder()
                .host(mockServer.hostName)
                .port(mockServer.port)
                .scheme("http")
                .build()
                .also {
                    Log.w(javaClass.simpleName, "--> Mockinized url: $it")
                }
            false -> request().url
        }

        return with(chain) {
            proceed(
                request().newBuilder()
                    .url(findServer())
                    .build()
            )
        }
    }

}


