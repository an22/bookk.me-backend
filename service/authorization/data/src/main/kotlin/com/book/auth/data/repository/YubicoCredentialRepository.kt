package com.book.auth.data.repository

import com.yubico.webauthn.CredentialRepository
import com.yubico.webauthn.RegisteredCredential
import com.yubico.webauthn.data.ByteArray
import com.yubico.webauthn.data.PublicKeyCredentialDescriptor
import java.util.*


internal class YubicoCredentialRepository : CredentialRepository {

    override fun getCredentialIdsForUsername(username: String): MutableSet<PublicKeyCredentialDescriptor> {
        return mutableSetOf()
    }

    override fun getUserHandleForUsername(username: String): Optional<ByteArray> {
        return Optional.empty()
    }

    override fun getUsernameForUserHandle(handle: ByteArray): Optional<String> {
        return Optional.empty()
    }

    override fun lookup(credentialId: ByteArray, handle: ByteArray?): Optional<RegisteredCredential> {
        return Optional.empty()
    }

    override fun lookupAll(credentialId: ByteArray): MutableSet<RegisteredCredential> {
        return mutableSetOf()
    }

//    private fun getLoginChallenge(): String {
//        val request = rp.startAssertion(
//            StartAssertionOptions.builder()
//                .build()
//        ).toCredentialsGetJson()
//
//        return request
//    }
//
//    private suspend fun validateLogin(publicKeyCredentialJson: String) {
//        val pkc = PublicKeyCredential.parseAssertionResponseJson(publicKeyCredentialJson)
//        val request = cacheClient.get<_, String>(pkc.id.toString())
//        try {
//            val result = rp.finishAssertion(
//                FinishAssertionOptions.builder()
//                    .request(AssertionRequest.fromJson(request)) // The PublicKeyCredentialRequestOptions from startAssertion above
//                    .response(pkc)
//                    .build()
//            )
//
//            if (result.isSuccess) {
//
//            }
//        } catch (e: AssertionFailedException) { /* ... */
//        }
//    }
}