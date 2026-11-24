package com.example.api.exception

import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {
    @ExceptionHandler(ItemNotFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun handleItemNotFound(ex: ItemNotFoundException): ProblemDetail = problem(HttpStatus.NOT_FOUND, ex)

    @ExceptionHandler(InvalidPagingException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleInvalidPaging(ex: InvalidPagingException): ProblemDetail = problem(HttpStatus.BAD_REQUEST, ex)

    @ExceptionHandler(InvalidCredentialsException::class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    fun handleInvalidCredentials(ex: InvalidCredentialsException): ProblemDetail = problem(HttpStatus.UNAUTHORIZED, ex)

    private fun problem(
        status: HttpStatus,
        ex: Exception,
    ): ProblemDetail = ProblemDetail.forStatusAndDetail(status, ex.message ?: status.reasonPhrase)
}
