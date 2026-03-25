package com.gaming.ballbuzz.domain

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.russhwolf.settings.ObservableSettings
import com.gaming.ballbuzz.util.Platform
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.random.Random

const val SCORE_KEY = "score"

data class Game(
    val platform: Platform,
    val screenWidth: Int = 0,
    val screenHeight: Int = 0,
    val gravity: Float = if (platform == Platform.Android) 0.8f else if (platform == Platform.iOS) 0.8f else 0.25f,
    val ballRadius: Float = 30f,
    val ballJumpImpulse: Float = if (platform == Platform.Android) -12f else if (platform == Platform.iOS) -12f else -8f,
    val ballMaxVelocity: Float = if (platform == Platform.Android) 25f else if(platform == Platform.iOS) 20f else 20f,
    val pipeWidth: Float = 150f,
    val pipeVelocity: Float = if (platform == Platform.Android) 5f else if(platform == Platform.iOS) 7f else 2.5f,
    val pipeGapSize: Float = if (platform == Platform.Android) 250f else 300f
) : KoinComponent {
    private val audioPlayer: AudioPlayer by inject()
    private val settings: ObservableSettings by inject()
    var status by mutableStateOf(GameStatus.Idle)
        private set
    var ballVelocity by mutableStateOf(0f)
        private set
    var ball by mutableStateOf(
        Ball(
            x = (screenWidth / 4).toFloat(),
            y = (screenHeight / 2).toFloat(),
            radius = ballRadius
        )
    )
        private set
    var pipePairs = mutableStateListOf<PipePair>()
    var currentScore by mutableStateOf(0)
        private set
    var bestScore by mutableStateOf(0)
        private set
    private var isFallingSoundPlayed = false

    init {
        bestScore = settings.getInt(
            key = SCORE_KEY,
            defaultValue = 0
        )
        settings.addIntListener(
            key = SCORE_KEY,
            defaultValue = 0
        ) {
            bestScore = it
        }
    }

    fun start() {
        status = GameStatus.Started
        audioPlayer.playGameSoundInLoop()
    }

    fun gameOver() {
        status = GameStatus.Over
        audioPlayer.stopGameSound()
        saveScore()
        isFallingSoundPlayed = false
    }

    private fun saveScore() {
        if (bestScore < currentScore) {
            settings.putInt(key = SCORE_KEY, value = currentScore)
            bestScore = currentScore
        }
    }

    fun jump() {
        ballVelocity = ballJumpImpulse
        audioPlayer.playJumpSound()
        isFallingSoundPlayed = false
    }

    fun restart() {
        resetballPosition()
        removePipes()
        resetScore()
        start()
        isFallingSoundPlayed = false
    }

    private fun resetballPosition() {
        ball = ball.copy(y = (screenHeight / 2).toFloat())
        ballVelocity = 0f
    }

    private fun removePipes() {
        pipePairs.clear()
    }

    private fun resetScore() {
        currentScore = 0
    }

    fun updateGameProgress() {
        pipePairs.forEach { pipePair ->
            if (isCollision(pipePair = pipePair)) {
                gameOver()
                return
            }

            if (!pipePair.scored && ball.x > pipePair.x + pipeWidth / 2) {
                pipePair.scored = true
                currentScore += 1
            }
        }

        if (ball.y < 0) {
            stopTheball()
            return
        } else if (ball.y > screenHeight) {
            gameOver()
            return
        }

        ballVelocity = (ballVelocity + gravity)
            .coerceIn(-ballMaxVelocity, ballMaxVelocity)
        ball = ball.copy(y = ball.y + ballVelocity)

        // When to play the falling sound
        if (ballVelocity > (ballMaxVelocity / 1.1)) {
            if (!isFallingSoundPlayed) {
                audioPlayer.playFallingSound()
                isFallingSoundPlayed = true
            }
        }

        spawnPipes()
    }

    private fun spawnPipes() {
        pipePairs.forEach { it.x -= pipeVelocity }
        pipePairs.removeAll { it.x + pipeWidth < 0 }

        val isLandscape = screenWidth > screenHeight
        val spawnThreshold = if (isLandscape) screenWidth / 1.25
        else screenWidth / 2.0

        if (pipePairs.isEmpty() || pipePairs.last().x < spawnThreshold) {
            val initialPipeX = screenWidth.toFloat() + pipeWidth
            val topHeight = Random.nextFloat() * (screenHeight / 2)
            val bottomHeight = screenHeight - topHeight - pipeGapSize
            val newPipePair = PipePair(
                x = initialPipeX,
                y = topHeight + pipeGapSize / 2,
                topHeight = topHeight,
                bottomHeight = bottomHeight
            )
            pipePairs.add(newPipePair)
        }
    }

    private fun isCollision(pipePair: PipePair): Boolean {
        // Check horizontal collision. ball overlaps the Pipe's X range.
        val ballRightEdge = ball.x + ball.radius
        val ballLeftEdge = ball.x - ball.radius
        val pipeLeftEdge = pipePair.x - pipeWidth / 2
        val pipeRightEdge = pipePair.x + pipeWidth / 2
        val horizontalCollision = ballRightEdge > pipeLeftEdge
                && ballLeftEdge < pipeRightEdge

        // Check if ball is within the vertical gap.
        val ballTopEdge = ball.y - ball.radius
        val ballBottomEdge = ball.y + ball.radius
        val gapTopEdge = pipePair.y - pipeGapSize / 2
        val gapBottomEdge = pipePair.y + pipeGapSize / 2
        val ballInGap = ballTopEdge > gapTopEdge
                && ballBottomEdge < gapBottomEdge

        return horizontalCollision && !ballInGap
    }

    private fun stopTheball() {
        ballVelocity = 0f
        ball = ball.copy(y = 0f)
    }

    fun cleanup() {
        audioPlayer.release()
    }
}