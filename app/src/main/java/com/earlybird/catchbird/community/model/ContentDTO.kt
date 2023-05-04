package com.earlybird.catchbird.community.model

import java.util.HashMap

data class ContentDTO(var explain: String? = null,
                      var imageUrl: String? = null,
                      var uid: String? = null,
                      var userId: String? = null,
                      var nickname: String? = null,
                      var timestamp: Long? = null,
                      var favoriteCount: Int = 0,
                      var favorites: MutableMap<String, Boolean> = HashMap()) {

    data class Comment(var uid: String? = null,
                       var userId: String? = null,
                       var nickname: String? = null,
                       var comment: String? = null,
                       var timestamp: Long? = null)
}

