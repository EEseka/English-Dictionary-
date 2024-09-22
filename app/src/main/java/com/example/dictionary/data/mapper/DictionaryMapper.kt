package com.example.dictionary.data.mapper

import com.example.dictionary.data.local.entity.DefinitionEntity
import com.example.dictionary.data.local.entity.MeaningEntity
import com.example.dictionary.data.local.entity.WordInfoEntity
import com.example.dictionary.data.local.relations.MeaningWithDefinitions
import com.example.dictionary.data.local.relations.WordInfoWithMeaningsAndDefinitions
import com.example.dictionary.data.remote.dto.DefinitionDto
import com.example.dictionary.data.remote.dto.WordInfoDto
import com.example.dictionary.domain.model.Definition
import com.example.dictionary.domain.model.Meaning
import com.example.dictionary.domain.model.WordInfo
import java.util.UUID

fun WordInfoDto.toWordInfoWithMeaningsAndDefinitions(): WordInfoWithMeaningsAndDefinitions {
    // Generate a unique ID for the word
    val wordId = UUID.randomUUID().toString()
    // Check all the Phonetics list and return the Phonetic that contains an audio file
    val text = if (phonetics.isNotEmpty()) {
        phonetics.find { it.audio != "" }?.text ?: phonetics[0].text
    } else ""
    val audio = if (phonetics.isNotEmpty()) {
        phonetics.find { it.audio != "" }?.audio ?: phonetics[0].audio
    } else ""

    val wordInfoEntity = WordInfoEntity(
        wordId = wordId,
        word = word ?: "No definitions found, please check your spelling",
        text = text ?: "",
        audio = audio ?: "",
    )

    val meaningWithDefinitions = meanings.map { meaningDto ->
        // Generate a unique ID for each meaning
        val meaningId = UUID.randomUUID().toString()
        MeaningWithDefinitions(
            meaning = MeaningEntity(
                meaningId = meaningId,
                wordId = wordId,
                partOfSpeech = meaningDto.partOfSpeech ?: "Part of speech unavailable",
                synonyms = meaningDto.synonyms,
                antonyms = meaningDto.antonyms
            ),
            definitions = meaningDto.definitions.map { definitionDto ->
                definitionDto.toDefinitionEntity(meaningId)
            }
        )
    }

    return WordInfoWithMeaningsAndDefinitions(wordInfoEntity, meaningWithDefinitions)
}

fun WordInfoWithMeaningsAndDefinitions.toWordInfo(isLiked: Boolean): WordInfo {
    return WordInfo(
        wordId = word.wordId,
        word = word.word,
        text = word.text,
        audio = word.audio,
        meanings = meanings.map { meaningWithDefinitions ->
            meaningWithDefinitions.toMeaning()
        },
        isLiked = isLiked
    )
}

fun WordInfo.toWordInfoWithMeaningsAndDefinitions(): WordInfoWithMeaningsAndDefinitions {
    val wordInfoEntity = WordInfoEntity(
        wordId = wordId,
        word = word,
        text = text,
        audio = audio,
    )
    val meaningWithDefinitions = meanings.map { meaning ->
        MeaningWithDefinitions(
            meaning = MeaningEntity(
                meaningId = meaning.meaningId,
                wordId = wordInfoEntity.wordId,
                partOfSpeech = meaning.partOfSpeech,
                synonyms = meaning.synonyms,
                antonyms = meaning.antonyms
            ),
            definitions = meaning.definitions.map {
                DefinitionEntity(
                    meaningId = meaning.meaningId,
                    definition = it.definition,
                    example = it.example
                )
            }
        )
    }
    return WordInfoWithMeaningsAndDefinitions(wordInfoEntity, meaningWithDefinitions)
}

private fun MeaningWithDefinitions.toMeaning(): Meaning {
    return Meaning(
        meaningId = meaning.meaningId,
        partOfSpeech = meaning.partOfSpeech,
        definitions = definitions.map { it.toDefinition() },
        synonyms = meaning.synonyms,
        antonyms = meaning.antonyms
    )
}

private fun DefinitionEntity.toDefinition(): Definition {
    return Definition(
        definition = definition,
        example = example
    )
}

private fun DefinitionDto.toDefinitionEntity(meaningId: String): DefinitionEntity {
    return DefinitionEntity(
        meaningId = meaningId,
        definition = definition ?: "No definition available",
        example = example ?: ""
    )
}