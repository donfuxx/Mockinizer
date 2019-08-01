package com.appham.mockinizer

import android.os.SystemClock
import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.HTTP
import java.io.IOException
import java.net.*


private const val BASE_URL = BuildConfig.BASE_URL;

class ApiService {

    /**
     * Create api interface for posts api
     */
    val demoApi: DemoApi by lazy {
        retrofit.create(DemoApi::class.java)
    }

    /**
     * Http client with logging enabled
     */
    private val okHttpClient by lazy {
        SystemClock.sleep(2000)
        val loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(MockinizerInterceptor(mocks)) //TODO: does this work with Pie?
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
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()
    }


}