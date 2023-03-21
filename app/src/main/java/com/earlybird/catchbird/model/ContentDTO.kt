package com.earlybird.catchbird.model

data class ContentDTO(
    var explain: String? = null,
    var imageUrl: String? = null,
    var uid: String? = null,
    var userId: String? = null,
    var timestamp: Long? = null,
    var favoriteCount: Int = 0
    //var favorites: Map<String, Boolean> = HashMap
    ) {


    data class Commment(
        var uid: String? = null,
    var userId: String? = null,
    var timestamp: Long? = null)
}

