package com.tech.ssylix.lyght_reader.data.models

import androidx.annotation.Keep

@Keep
data class Summary(var sumText: String, var subSum : ArrayList<Summary>?, var position: Int?){

    constructor() : this("", null, null)
}