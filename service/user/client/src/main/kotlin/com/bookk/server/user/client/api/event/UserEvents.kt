package com.bookk.server.user.client.api.event

import com.bookk.core.data.eventstreaming.EventStreaming

sealed interface UserEvents : EventStreaming.Event<String> {

}