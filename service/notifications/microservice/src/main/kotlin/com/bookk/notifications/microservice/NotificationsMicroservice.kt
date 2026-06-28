package com.bookk.notifications.microservice

import com.bookk.core.AppLevelConstants
import com.bookk.core.data.cache.impl.di.cacheModule
import com.bookk.core.data.eventstreaming.di.eventStreamingModule
import com.bookk.core.data.eventstreaming.startEventHandling
import com.bookk.core.service.installNegotiation
import com.bookk.core.service.startServer
import com.bookk.notifications.data.di.notificationsDataModule
import com.bookk.notifications.domain.impl.di.notificationsDomainModule
import com.bookk.notifications.microservice.route.notificationsRoute
import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import org.koin.dsl.module

fun notificationsModule() = module {
    includes(
        notificationsDomainModule(),
        notificationsDataModule(),
        eventStreamingModule(),
        cacheModule()
    )
}

fun main() {
    initFirebase()
    startServer(diModules = listOf(notificationsModule())) { _ ->
        installNegotiation()
        startEventHandling()
        notificationsRoute()
    }
}

private fun initFirebase() {
    val options = FirebaseOptions.builder()
        .setCredentials(GoogleCredentials.fromStream(AppLevelConstants.firebasePrivateKey.byteInputStream()))
        .build()

    if (FirebaseApp.getApps().isEmpty()) {
        FirebaseApp.initializeApp(options)
    }
}
