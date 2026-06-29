package library.signing.impl

import com.bookk.core.domain.datasource.transaction.TransactionManager
import library.signing.GetActiveSigningKey
import library.signing.SigningKey
import library.signing.SigningKeyDataSource
import library.signing.impl.key.RsaSigningKeyFactory

internal class GetActiveSigningKeyImpl(
    private val signingKeyDataSource: SigningKeyDataSource,
    private val transactionManager: TransactionManager
) : GetActiveSigningKey {

    override suspend fun invoke(): Result<SigningKey> = transactionManager.transaction {
        signingKeyDataSource.getActiveKey() ?: run {
            val (publicKeyPem, privateKeyPem) = RsaSigningKeyFactory.generate()
            signingKeyDataSource.insertKey(publicKeyPem, privateKeyPem)
        }
    }
}
