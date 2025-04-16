package com.bookk.core

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Returns the hexadecimal string representation of this uuid without hyphens.
 *
 * The resulting string is in lowercase and consists of 32 characters. Each hexadecimal digit
 * in the string sequentially represents the next 4 bits of the uuid, starting from the most
 * significant 4 bits in the first digit to the least significant 4 bits in the last digit.
 **/
@OptIn(ExperimentalUuidApi::class)
fun newRandomUUIDString(): String = Uuid.random().toHexString()

@OptIn(ExperimentalUuidApi::class)
fun newRandomUUIDByteArray(): ByteArray = Uuid.random().toByteArray()

@OptIn(ExperimentalUuidApi::class)
fun ByteArray.toHexUUID(): String = Uuid.fromByteArray(this).toHexString()

@OptIn(ExperimentalUuidApi::class)
fun String.toUUIDBytes(): ByteArray = Uuid.parseHex(this).toByteArray()