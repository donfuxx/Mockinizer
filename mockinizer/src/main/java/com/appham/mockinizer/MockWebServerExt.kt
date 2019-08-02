package com.appham.mockinizer

import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers
import okhttp3.mockwebserver.MockWebServer
import okhttp3.tls.HandshakeCertificates
import okhttp3.tls.HeldCertificate

internal fun MockWebServer.configure(): MockWebServer {
    Completable.fromAction { start() }
        .onErrorComplete()
        .subscribeOn(Schedulers.io())
        .subscribe()

    val localhostCertificate = HeldCertificate.Builder()
        .addSubjectAlternativeName("127.0.0.1")
        .build()

    val serverCertificates = HandshakeCertificates.Builder()
        .heldCertificate(localhostCertificate)
        .build()

    useHttps(serverCertificates.sslSocketFactory(), false)
    return this
}