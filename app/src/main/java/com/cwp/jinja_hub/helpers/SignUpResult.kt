package com.cwp.jinja_hub.helpers

sealed class SignUpResult {
    data class Success(val userId: String) : SignUpResult()
    data class Error(val errorMessage: String) : SignUpResult()
}