package com.example.dictionary.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class WordInfoDto(
    @field:Json(name = "word") val word: String?,
    @field:Json(name = "phonetics") val phonetics: List<PhoneticDto>,
    @field:Json(name = "meanings") val meanings: List<MeaningDto>
)

@JsonClass(generateAdapter = true)
data class PhoneticDto(
    @field:Json(name = "text") val text: String?,
    @field:Json(name = "audio") val audio: String?
)

@JsonClass(generateAdapter = true)
data class MeaningDto(
    @field:Json(name = "partOfSpeech") val partOfSpeech: String?,
    @field:Json(name = "definitions") val definitions: List<DefinitionDto>,
    @field:Json(name = "synonyms") val synonyms: List<String>,
    @field:Json(name = "antonyms") val antonyms: List<String>
)

@JsonClass(generateAdapter = true)
data class DefinitionDto(
    @field:Json(name = "definition") val definition: String?,
    @field:Json(name = "example") val example: String?
)
