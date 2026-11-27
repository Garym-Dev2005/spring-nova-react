package com.example.api.exception

/** One generic message for unknown user, bad password and disabled account. */
class InvalidCredentialsException : RuntimeException("Invalid username or password")
