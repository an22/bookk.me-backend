package com.bookk.appointments.domain.api.entity

object AppointmentErrorCodes {
    private const val BASE = 300000

    const val REQUEST_EXISTS = BASE + 1
    const val TIME_NOT_ALLOWED = BASE + 2
    const val DATE_NOT_ALLOWED = BASE + 3
    const val APPOINTMENT_EXISTS = BASE + 4
    const val APPOINTMENT_ALREADY_CANCELED = BASE + 5
    const val APPOINTMENT_ALREADY_COMPLETED = BASE + 6
    const val REQUEST_ALREADY_DECLINED = BASE + 7
    const val REQUEST_ALREADY_APPROVED = BASE + 8
    const val PLUGIN_ALREADY_ENABLED = BASE + 9
    const val ACTIVE_DAY_WITHOUT_WORK_HOURS = BASE + 10
    const val INVALID_DAY_OFF_RANGE = BASE + 11
}