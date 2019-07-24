package com.tech.ssylix.lyght_reader.data.models

import androidx.annotation.Keep

@Keep
data class Reference(var title: String, var notes : String, var position : Float?, var type: Type){

    var url : String? = null
    var mDocId : String? = null
    var mPageId : Int? = null

    constructor() : this("", "", null, Type.OTHER)
}