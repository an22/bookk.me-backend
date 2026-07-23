package library.signing.impl.orm.entity

import com.bookk.core.data.DecoratorUUIDEntityClass
import library.signing.impl.orm.table.SigningKeyTable
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.UUIDEntity
import java.util.UUID

internal class SigningKeyEntity(id: EntityID<UUID>) : UUIDEntity(id) {

    var publicKey by SigningKeyTable.publicKey
    var privateKey by SigningKeyTable.privateKey
    var status by SigningKeyTable.status
    var createdAt by SigningKeyTable.createdAt
    var retiredAt by SigningKeyTable.retiredAt

    companion object : DecoratorUUIDEntityClass<SigningKeyEntity>(SigningKeyTable)
}
