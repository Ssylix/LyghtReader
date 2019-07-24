package com.tech.ssylix.lyght_reader.data.models

import androidx.annotation.Keep

@Keep
data class Media(
    var title: String,
    var storageLocation: String,
    var rating: String?,
    var metadata: Metadata?,
    var tagData: Any?,
    var type: Type
){
    var stringId : String? = null
    constructor() : this("", "", "", null, "", Type.OTHER)
}