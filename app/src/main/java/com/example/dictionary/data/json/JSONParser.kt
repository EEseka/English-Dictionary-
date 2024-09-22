package com.example.dictionary.data.json

import java.io.InputStream

interface JSONParser<T> {
    suspend fun parse(inputStream: InputStream): List<T>
}