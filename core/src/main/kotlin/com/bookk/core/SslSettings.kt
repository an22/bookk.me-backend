package com.bookk.core

import java.security.KeyStore
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

object SslSettings {
    private fun getKeyStore(): KeyStore {
        val keystoreStream = javaClass.classLoader.getResourceAsStream(System.getenv("BOOKK_ME_SERVICE_SSL_FILE"))
        val keyStorePass = System.getenv("BOOKK_ME_SERVICE_SSL_PASSWORD")
        return KeyStore.getInstance(KeyStore.getDefaultType()).apply {
            load(keystoreStream, keyStorePass.toCharArray())
        }
    }

    private fun getTrustManagerFactory(): TrustManagerFactory? {
        val trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
        trustManagerFactory.init(getKeyStore())
        return trustManagerFactory
    }

    fun getTrustManager(): X509TrustManager {
        return getTrustManagerFactory()?.trustManagers?.first { it is X509TrustManager } as X509TrustManager
    }
}