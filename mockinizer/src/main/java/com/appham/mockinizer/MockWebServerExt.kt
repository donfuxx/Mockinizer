package com.appham.mockinizer

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.mockwebserver.MockWebServer
import okhttp3.tls.HandshakeCertificates
import okhttp3.tls.HeldCertificate

internal fun MockWebServer.configure(): MockWebServer {

    GlobalScope.launch {
        start(BuildConfig.MOCKSERVER_PORT)
    }

    val localhostCertificate = HeldCertificate.Builder()
        .addSubjectAlternativeName("127.0.0.1")
        .build()

    val serverCertificates = HandshakeCertificates.Builder()
        .heldCertificate(localhostCertificate)
        .build()

    useHttps(serverCertificates.sslSocketFactory(), false)
    return this
}