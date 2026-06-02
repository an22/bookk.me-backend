package com.bookk.appointments.domain.impl.di

import com.bookk.appointments.domain.api.operation.CreateAppointment
import com.bookk.appointments.domain.api.operation.EnableAppointmentsForBusiness
import com.bookk.appointments.domain.impl.operation.CreateAppointmentImpl
import com.bookk.appointments.domain.impl.operation.EnableAppointmentsForBusinessImpl
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module

fun appointmentsDomainModule() = module {
    factoryOf(::CreateAppointmentImpl) bind CreateAppointment::class
    factoryOf(::EnableAppointmentsForBusinessImpl) bind EnableAppointmentsForBusiness::class
}