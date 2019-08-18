package com.appham.mockinizer

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


private const val BASE_URL = BuildConfig.BASE_URL;

object TestApiService {

    /**
     * Create api interface for posts api
     */
    val testApi: TestApi by lazy {
        retrofit.create(TestApi::class.java)
    }

    /**
     * Http client with logging enabled
     */
    private val okHttpClient by lazy {
        val loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .mockinize(mocks)
            .build()
    }

    /**
     * Retrofit instance with Rxjava adapter
     */
    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }


}