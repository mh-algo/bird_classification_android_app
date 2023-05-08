package com.earlybird.catchbird

data class Bird(
    val image: Int,
    val name: String,
    val info: String,
    val isRegist: Boolean
)
data class Rank(
    val rank: Int,
    val profileImage: Int,
    val userId: String,
    val score: Int
)
data class SearchBird(
    val name: String,
    val image: Int
)
data class BirdInfo(
    val image: Int,
    val info: String
)
data class Regist(
    val image: String,
    val date: String,
    val time: String
)
