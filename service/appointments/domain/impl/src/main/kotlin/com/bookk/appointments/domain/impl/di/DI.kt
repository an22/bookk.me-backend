package com.bookk.appointments.domain.impl.di

import com.bookk.appointments.domain.api.operation.CreateAppointment
import com.bookk.appointments.domain.api.operation.CreateAppointmentRequest
import com.bookk.appointments.domain.api.operation.DeleteModule
import com.bookk.appointments.domain.api.operation.EditSettings
import com.bookk.appointments.domain.api.operation.EnableAppointmentsForBusiness
import com.bookk.appointments.domain.api.operation.GetAppointmentRequests
import com.bookk.appointments.domain.api.operation.GetAppointments
import com.bookk.appointments.domain.api.operation.UpdateAppointment
import com.bookk.appointments.domain.impl.event.AppointmentEventHandler
import com.bookk.appointments.domain.impl.operation.CreateAppointmentImpl
import com.bookk.appointments.domain.impl.operation.CreateAppointmentRequestImpl
import com.bookk.appointments.domain.impl.operation.DeleteModuleImpl
import com.bookk.appointments.domain.impl.operation.EditSettingsImpl
import com.bookk.appointments.domain.impl.operation.EnableAppointmentsForBusinessImpl
import com.bookk.appointments.domain.impl.operation.GetAppointmentRequestsImpl
import com.bookk.appointments.domain.impl.operation.GetAppointmentsImpl
import com.bookk.appointments.domain.impl.operation.UpdateAppointmentImpl
import com.bookk.core.data.eventstreaming.EventHandler
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module

fun appointmentsDomainModule() = module {
    factoryOf(::CreateAppointmentImpl) bind CreateAppointment::class
    factoryOf(::EnableAppointmentsForBusinessImpl) bind EnableAppointmentsForBusiness::class
    factoryOf(::AppointmentEventHandler) bind EventHandler::class
    factoryOf(::DeleteModuleImpl) bind DeleteModule::class
    factoryOf(::EditSettingsImpl) bind EditSettings::class
    factoryOf(::CreateAppointmentRequestImpl) bind CreateAppointmentRequest::class
    factoryOf(::GetAppointmentRequestsImpl) bind GetAppointmentRequests::class
    factoryOf(::GetAppointmentsImpl) bind GetAppointments::class
    factoryOf(::UpdateAppointmentImpl) bind UpdateAppointment::class
}