package com.geeky.flyingbee

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform