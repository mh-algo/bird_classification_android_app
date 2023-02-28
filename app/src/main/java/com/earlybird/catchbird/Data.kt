package com.earlybird.catchbird

data class Bird(
    val image: Int,
    val name: String,
    val info: String,
    val isRegist: Boolean
)
data class Rank(
    val rank: Int,
    val userId: String,
    val score: Int
)