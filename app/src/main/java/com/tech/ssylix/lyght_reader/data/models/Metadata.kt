package com.tech.ssylix.lyght_reader.data.models

import androidx.annotation.Keep

@Keep
data class Metadata(
    var downloadUrl: String?,
    var thumbnailUrl : String?,
    var type: String?
){
    constructor() : this(null, null, null)
}