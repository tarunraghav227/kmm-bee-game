package com.gaming.ballbuzz

import androidx.compose.ui.window.ComposeUIViewController
import com.gaming.ballbuzz.di.initializeKoin

fun MainViewController() = ComposeUIViewController(
    configure = { initializeKoin() }
) { App() }