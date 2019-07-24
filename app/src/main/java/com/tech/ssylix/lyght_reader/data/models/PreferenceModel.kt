package com.tech.ssylix.lyght_reader.data.models

import androidx.annotation.Keep

@Keep
data class PreferenceModel(
    val userPreferenceInfo : Any?
){
    constructor() : this(null)
}