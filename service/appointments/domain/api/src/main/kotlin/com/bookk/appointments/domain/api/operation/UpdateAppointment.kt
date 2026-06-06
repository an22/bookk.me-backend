package com.bookk.appointments.domain.api.operation

import com.bookk.appointments.domain.api.entity.Appointment

interface UpdateAppointment {
    suspend operator fun invoke(appointment: Appointment): Appointment
}