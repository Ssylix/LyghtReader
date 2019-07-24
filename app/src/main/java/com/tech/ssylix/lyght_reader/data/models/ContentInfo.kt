package com.tech.ssylix.lyght_reader.data.models

import androidx.annotation.Keep

@Keep
data class ContentInfo(
    var viewed : InfoCollection? = InfoCollection(),
    var uploads: InfoCollection? = InfoCollection()
){
    constructor() : this(
        InfoCollection(),
        InfoCollection()
    )
}