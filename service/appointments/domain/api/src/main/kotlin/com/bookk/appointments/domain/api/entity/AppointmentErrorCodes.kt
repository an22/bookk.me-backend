package com.bookk.appointments.domain.api.entity

object AppointmentErrorCodes {
    private const val BASE = 300000

    const val REQUEST_EXISTS = BASE + 1
    const val TIME_NOT_ALLOWED = BASE + 2
}