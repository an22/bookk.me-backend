package library.signing.impl.orm.table

import org.jetbrains.exposed.v1.core.Table

fun signingKeyTables(): Array<Table> = arrayOf(SigningKeyTable)
