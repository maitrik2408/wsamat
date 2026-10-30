package com.kirtiam.wsamat.data.model

sealed class Result<out T> {
    data class Success<out T>(val data: T) : Result<T>()
    data class Error(val exception: Throwable, val message: String = exception.localizedMessage ?: "An unexpected error occurred") : Result<Nothing>()
    object Loading : Result<Nothing>()
}
