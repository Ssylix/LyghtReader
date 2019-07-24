package com.tech.ssylix.lyght_reader.data.models

import androidx.annotation.Keep

@Keep
data class InfoCollection(
    var docs : ArrayList<Document> = ArrayList(),
    var videos: ArrayList<Media> = ArrayList(),
    var audios : ArrayList<Media> = ArrayList(),
    var photos: ArrayList<Media> = ArrayList()
){
    constructor() : this(ArrayList(), ArrayList(), ArrayList(), ArrayList())
}