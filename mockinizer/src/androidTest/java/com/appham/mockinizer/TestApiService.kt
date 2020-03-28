package com.appham.mockinizer

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.mockwebserver.MockWebServer
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


internal const val BASE_URL = "https://my-json-server.typicode.com/typicode/demo/"

object TestApiService {

    /**
     * Create api interface for posts api
     */
    val testApi: TestApi = {

        val mockWebServer: MockWebServer = MockWebServer().configure()

        val okHttpClient by lazy {
            val loggingInterceptor = HttpLoggingInterceptor()
            loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
            OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .mockinize(mocks, mockWebServer)
                .build()
        }

        val retrofit by lazy {
            Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }

        retrofit.create(TestApi::class.java)
    }()

}