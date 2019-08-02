package com.appham.mockinizer

import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import java.security.cert.X509Certificate
import javax.net.ssl.*

fun OkHttpClient.Builder.mockinize(
    mocks: Map<RequestFilter, MockResponse> = mapOf(),
    trustManagers: Array<TrustManager> = getAllTrustingManagers(),
    socketFactory: SSLSocketFactory = getSslSocketFactory(trustManagers),
    hostnameVerifier: HostnameVerifier = HostnameVerifier { _, _ -> true }
): OkHttpClient.Builder {
    addInterceptor(MockinizerInterceptor(mocks))
        .sslSocketFactory(socketFactory, trustManagers[0] as X509TrustManager)
        .hostnameVerifier(hostnameVerifier)
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