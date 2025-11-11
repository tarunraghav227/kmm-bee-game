package com.tarun.mybeeapplication

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform