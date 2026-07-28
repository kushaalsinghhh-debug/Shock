package com.example

import com.example.data.repository.WinX7Repository
import com.google.firebase.auth.FirebaseAuthException
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WinX7AuthValidationTest {

    @Test
    fun validEmailAddressesPassValidation() {
        assertTrue(WinX7Repository.isValidEmail("player@example.com"))
        assertTrue(WinX7Repository.isValidEmail("user.name+tag@sub.domain.co"))
    }

    @Test
    fun invalidEmailAddressesFailValidation() {
        assertFalse(WinX7Repository.isValidEmail(""))
        assertFalse(WinX7Repository.isValidEmail("abc123"))
        assertFalse(WinX7Repository.isValidEmail("user@"))
    }

    @Test
    fun firebaseAuthErrorsAreMappedToFriendlyMessages() {
        val invalidEmail = FirebaseAuthException("ERROR_INVALID_EMAIL", "Invalid email")
        val duplicateEmail = FirebaseAuthException("ERROR_EMAIL_ALREADY_IN_USE", "Email already in use")
        val wrongPassword = FirebaseAuthException("ERROR_WRONG_PASSWORD", "Wrong password")

        assertTrue(WinX7Repository.getFriendlyAuthErrorMessage(invalidEmail, isLogin = false).contains("valid email", ignoreCase = true))
        assertTrue(WinX7Repository.getFriendlyAuthErrorMessage(duplicateEmail, isLogin = false).contains("already exists", ignoreCase = true))
        assertTrue(WinX7Repository.getFriendlyAuthErrorMessage(wrongPassword, isLogin = true).contains("incorrect", ignoreCase = true))
    }
}
