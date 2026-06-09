package com.bookk.appointments.data.di

import com.bookk.appointments.data.datasource.AppointmentDataSourceImpl
import com.bookk.appointments.data.datasource.AppointmentRequestDataSourceImpl
import com.bookk.appointments.data.datasource.AppointmentSettingsDataSourceImpl
import com.bookk.appointments.data.datasource.AppointmentSubscriptionDataSourceImpl
import com.bookk.appointments.data.datasource.PermissionsDataSourceImpl
import com.bookk.appointments.domain.datasource.AppointmentDataSource
import com.bookk.appointments.domain.datasource.AppointmentRequestDataSource
import com.bookk.appointments.domain.datasource.AppointmentSettingsDataSource
import com.bookk.appointments.domain.datasource.AppointmentSubscriptionDataSource
import com.bookk.appointments.domain.datasource.PermissionsDataSource
import com.bookk.core.data.database.createDatabase
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

fun appointmentsDataModule() = module {
    singleOf(::AppointmentDataSourceImpl) bind AppointmentDataSource::class
    singleOf(::AppointmentRequestDataSourceImpl) bind AppointmentRequestDataSource::class
    singleOf(::AppointmentSettingsDataSourceImpl) bind AppointmentSettingsDataSource::class
    singleOf(::AppointmentSubscriptionDataSourceImpl) bind AppointmentSubscriptionDataSource::class
    singleOf(::PermissionsDataSourceImpl) bind PermissionsDataSource::class
    createDatabase()
}