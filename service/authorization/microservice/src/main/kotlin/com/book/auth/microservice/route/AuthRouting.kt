package com.book.auth.microservice.route

import io.ktor.resources.Resource

object AuthRouting {
    @Resource("api")
    class Api {

        @Resource("/auth")
        class Auth(val parent: Api = Api()) {

            @Resource("/healthcheck")
            class HealthCheck(val parent: Auth = Auth())

            @Resource("/sign_in")
            class SignIn(val parent: Auth = Auth())

            @Resource("/email")
            class Email(val parent: Auth = Auth())

            @Resource("/passkey")
            class PassKey(val parent: Auth = Auth()) {
                @Resource("/challenge")
                class Challenge(val parent: PassKey = PassKey())
            }

            @Resource("/sign_up")
            class SignUp(val parent: Auth = Auth()) {
                @Resource("/passkey")
                class PassKey(val parent: SignUp = SignUp()) {
                    @Resource("/challenge")
                    class Challenge(val parent: PassKey = PassKey())

                    @Resource("/validate")
                    class Validate(val parent: PassKey = PassKey())
                }
            }

            @Resource("/refresh")
            class Refresh(val parent: Auth = Auth())

            @Resource("/session")
            class SignOut(val parent: Auth = Auth())

            @Resource("/account")
            class DeleteAccount(val parent: Auth = Auth())
        }
    }

}