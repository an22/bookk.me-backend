package com.bookk.appointments.domain.impl.di

import com.bookk.appointments.domain.api.operation.CancelAppointment
import com.bookk.appointments.domain.api.operation.CreateAppointment
import com.bookk.appointments.domain.api.operation.CreateAppointmentRequest
import com.bookk.appointments.domain.api.operation.DeclineAppointmentRequest
import com.bookk.appointments.domain.api.operation.DeleteModule
import com.bookk.appointments.domain.api.operation.EditSettings
import com.bookk.appointments.domain.api.operation.EnableAppointmentsForBusiness
import com.bookk.appointments.domain.api.operation.GetAppointmentHistory
import com.bookk.appointments.domain.api.operation.GetAppointmentRequests
import com.bookk.appointments.domain.api.operation.GetAppointmentsForDate
import com.bookk.appointments.domain.api.operation.IsAppointmentsEnabled
import com.bookk.appointments.domain.api.operation.MarkAppointmentsCompleted
import com.bookk.appointments.domain.api.operation.UpdateAppointment
import com.bookk.appointments.domain.impl.event.AppointmentEventHandler
import com.bookk.appointments.domain.impl.operation.CreateAppointmentImpl
import com.bookk.appointments.domain.impl.operation.CreateAppointmentRequestImpl
import com.bookk.appointments.domain.impl.operation.DeclineAppointmentImpl
import com.bookk.appointments.domain.impl.operation.DeclineAppointmentRequestImpl
import com.bookk.appointments.domain.impl.operation.DeleteModuleImpl
import com.bookk.appointments.domain.impl.operation.EditSettingsImpl
import com.bookk.appointments.domain.impl.operation.EnableAppointmentsForBusinessImpl
import com.bookk.appointments.domain.impl.operation.GetAppointmentHistoryImpl
import com.bookk.appointments.domain.impl.operation.GetAppointmentRequestsImpl
import com.bookk.appointments.domain.impl.operation.GetAppointmentsForDataImpl
import com.bookk.appointments.domain.impl.operation.IsAppointmentsEnabledImpl
import com.bookk.appointments.domain.impl.operation.MarkAppointmentsCompletedImpl
import com.bookk.appointments.domain.impl.operation.UpdateAppointmentImpl
import com.bookk.appointments.domain.impl.operation.UpdateBusinessInformation
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
    factoryOf(::GetAppointmentsForDataImpl) bind GetAppointmentsForDate::class
    factoryOf(::GetAppointmentHistoryImpl) bind GetAppointmentHistory::class
    factoryOf(::UpdateAppointmentImpl) bind UpdateAppointment::class
    factoryOf(::DeclineAppointmentImpl) bind CancelAppointment::class
    factoryOf(::UpdateBusinessInformation)
    factoryOf(::DeclineAppointmentRequestImpl) bind DeclineAppointmentRequest::class
    factoryOf(::IsAppointmentsEnabledImpl) bind IsAppointmentsEnabled::class
    factoryOf(::MarkAppointmentsCompletedImpl) bind MarkAppointmentsCompleted::class
}