package com.bookk.appointments.domain.api.entity

object AppointmentErrorCodes {
    private const val BASE = 300000

    const val REQUEST_EXISTS = BASE + 1
    const val TIME_NOT_ALLOWED = BASE + 2
    const val DATE_NOT_ALLOWED = BASE + 3
    const val APPOINTMENT_EXISTS = BASE + 4
    const val APPOINTMENT_ALREADY_CANCELED = BASE + 5
    const val APPOINTMENT_ALREADY_COMPLETED = BASE + 6
}