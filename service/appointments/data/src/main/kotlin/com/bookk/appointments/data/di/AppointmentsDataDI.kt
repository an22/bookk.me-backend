package com.bookk.appointments.data.di

import com.bookk.core.data.database.createDatabase
import org.koin.dsl.module

fun appointmentsDataModule() = module {

    createDatabase()
}