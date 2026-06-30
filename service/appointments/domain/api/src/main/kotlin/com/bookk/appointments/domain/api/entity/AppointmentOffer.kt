package com.bookk.appointments.domain.api.entity

import kotlinx.serialization.Serializable

@Serializable
class AppointmentOffer(
    val request: AppointmentRequest,
    val offerToken: String
) {
    companion object {
        fun stub(
            request: AppointmentRequest = AppointmentRequest.stub(),
            offerToken: String = "token"
        ) = AppointmentOffer(request = request, offerToken = offerToken)
    }
}