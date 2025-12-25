package com.bookk.core

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
fun ByteArray.toUUID(): Uuid = Uuid.fromByteArray(this)

@OptIn(ExperimentalUuidApi::class)
fun String.toUUID(): Uuid = Uuid.parse(this)