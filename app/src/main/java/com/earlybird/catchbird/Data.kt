package com.earlybird.catchbird

import android.provider.ContactsContract.CommonDataKinds.Nickname

data class Bird(
    val image: Int,
    val name: String,
    val info: String,
    val isRegist: Boolean
)
data class Rank(
    val rank: Int,
    val profileImage: String,
    val userNickname: String,
    val score: Int,
    val uid: String
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
