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

            @Resource("/sign_up")
            class SignUp(val parent: Auth = Auth())

            @Resource("/passkey")
            class PassKey(val parent: Auth = Auth()) {
                @Resource("/sign_in/challenge")
                class SignInChallenge(val parent: PassKey = PassKey())

                @Resource("/sign_up/challenge")
                class SignUpChallenge(val parent: PassKey = PassKey())

                @Resource("/add/challenge")
                class AddChallenge(val parent: PassKey = PassKey())

                @Resource("/add/finish")
                class AddFinish(val parent: PassKey = PassKey())

                @Resource("{id}")
                class Id(val parent: PassKey = PassKey(), val id: Long)
            }

            @Resource("/refresh")
            class Refresh(val parent: Auth = Auth())

            @Resource("/session")
            class SignOut(val parent: Auth = Auth())

            @Resource("/account")
            class Account(val parent: Auth = Auth())
        }
    }

}