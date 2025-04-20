package com.bookk.core

import java.io.File
import java.security.KeyStore
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

object SslSettings {
    private fun getKeyStore(): KeyStore {
        val keystoreFile = File(AppLevelConstants.sslFile)
        val keyStorePass = AppLevelConstants.sslPass
        return KeyStore.getInstance(
            keystoreFile,
            keyStorePass.toCharArray()
        )
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