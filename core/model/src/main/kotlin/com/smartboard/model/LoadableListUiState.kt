package com.smartboard.model

sealed class LoadableListUiState<out T> {
    data object Loading : LoadableListUiState<Nothing>()
    data class Success<T>(val items: List<T>) : LoadableListUiState<T>()
    data class Error(val message: String) : LoadableListUiState<Nothing>()
    data object Empty : LoadableListUiState<Nothing>()
}
