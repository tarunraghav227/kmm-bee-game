package com.gaming.ballbuzz.di

import com.gaming.ballbuzz.domain.AudioPlayer
import org.koin.dsl.module

actual val targetModule = module {
    single<AudioPlayer> { AudioPlayer() }
}