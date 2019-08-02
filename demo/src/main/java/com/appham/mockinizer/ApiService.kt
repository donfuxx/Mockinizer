package com.appham.mockinizer

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.security.cert.X509Certificate
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager


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

        // A non validating trustManager
        val allTrustingManagers = arrayOf<TrustManager>(object : X509TrustManager {

            override fun getAcceptedIssuers(): Array<X509Certificate> = emptyArray()

            override fun checkClientTrusted(
                chain: Array<X509Certificate>,
                authType: String
            ) {}

            override fun checkServerTrusted(
                chain: Array<X509Certificate>,
                authType: String
            ) {}
        })
        val sslContext = SSLContext.getInstance("SSL")
        sslContext.init(null, allTrustingManagers, java.security.SecureRandom())
        val sslSocketFactory = sslContext.socketFactory

        val loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .mockinize(mocks) //TODO: extension function for adding interceptor etc.
            .sslSocketFactory(sslSocketFactory, allTrustingManagers[0] as X509TrustManager)
            .hostnameVerifier(HostnameVerifier { _, _ -> true })
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