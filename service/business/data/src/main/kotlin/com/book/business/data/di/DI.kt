package com.book.business.data.di

import com.book.core.data.database.createDatabase
import org.koin.dsl.module

fun businessDataModule() = module {
    createDatabase(
        schemaName = System.getenv("BOOKK_ME_DB_SCHEME"),
        driver = System.getenv("BOOKK_ME_DB_DRIVER"),
        dbUrl = System.getenv("BOOKK_ME_DB_URL"),
        dbPort = System.getenv("BOOKK_ME_DB_PORT"),
        dbUsername = System.getenv("BOOKK_ME_DB_USER"),
        dbPassword = System.getenv("BOOKK_ME_DB_PASSWORD")
    )
}