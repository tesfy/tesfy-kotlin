package io.tesfy

interface Storage<T> {
    fun get(id: String): T?
    fun store(id: String, value: T)
}