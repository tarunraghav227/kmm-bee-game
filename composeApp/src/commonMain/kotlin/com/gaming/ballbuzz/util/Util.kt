package com.gaming.ballbuzz.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import ballbuzz.composeapp.generated.resources.Res
import ballbuzz.composeapp.generated.resources.chewy_regular
import org.jetbrains.compose.resources.Font

@Composable
fun ChewyFontFamily() = FontFamily(Font(Res.font.chewy_regular))