package com.appham.mockinizer

import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

fun OkHttpClient.Builder.mockinize(
    mocks: Map<RequestFilter, MockResponse> = mapOf(),
    trustManagers: Array<TrustManager> = getAllTrustingManagers(),
    socketFactory: SSLSocketFactory = getSslSocketFactory(trustManagers)

): OkHttpClient.Builder {
    this.addInterceptor(MockinizerInterceptor(mocks))
    this.sslSocketFactory(socketFactory, trustManagers[0] as X509TrustManager)
    return this
}

private fun getSslSocketFactory(trustManagers: Array<TrustManager>): SSLSocketFactory {
    val sslContext = SSLContext.getInstance("SSL")
    sslContext.init(null, trustManagers, java.security.SecureRandom())
    return sslContext.socketFactory
}

private fun getAllTrustingManagers(): Array<TrustManager> {
    return arrayOf(object : X509TrustManager {

        override fun getAcceptedIssuers(): Array<X509Certificate> = emptyArray()

        override fun checkClientTrusted(
            chain: Array<X509Certificate>,
            authType: String
        ) {
        }

        override fun checkServerTrusted(
            chain: Array<X509Certificate>,
            authType: String
        ) {
        }
    })
}