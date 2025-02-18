package com.book.auth.microservice.route.api.documentation.common

import com.book.auth.domain.api.authentication.operation.FinishAssertion.Error.ChallengeWindowExpired
import com.book.auth.domain.api.authentication.operation.FinishAssertion.Error.PasskeyOwnerNotFound
import com.book.auth.domain.api.authentication.operation.FinishAssertion.Error.VerificationFailed
import com.book.core.service.applyMediaType
import com.book.core.service.enity.SimpleServerError
import io.bkbn.kompendium.core.metadata.ResponseInfo
import io.bkbn.kompendium.enrichment.NumberEnrichment
import io.bkbn.kompendium.enrichment.ObjectEnrichment
import io.ktor.http.HttpStatusCode

internal fun ResponseInfo.Builder.challengeVerificationEnrichment() {
    applyMediaType()
    responseCode(HttpStatusCode.UnprocessableEntity)
    responseType<SimpleServerError>(ObjectEnrichment(id = "FinishAssertion") {
        SimpleServerError::errorCode {
            NumberEnrichment("errorCode") {
                minimum = 5
                maximum = 7
                description = buildString {
                    append("${ChallengeWindowExpired.code} - ${ChallengeWindowExpired.message}\n")
                    append("${PasskeyOwnerNotFound.code} - ${PasskeyOwnerNotFound.message}\n")
                    append("${VerificationFailed.code} - ${VerificationFailed.message}\n")
                }
            }
        }
    })
    description("Unprocessable entity")
}