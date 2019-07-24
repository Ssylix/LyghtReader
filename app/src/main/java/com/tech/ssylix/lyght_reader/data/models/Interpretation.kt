package com.tech.ssylix.lyght_reader.data.models

import androidx.annotation.Keep

@Keep
data class Interpretation(var capText : String, var subText : String, var subInterpretations : ArrayList<Interpretation>?,
                          var position : Float, var rating : Int){
    var mDocId : String? = null
    var mPageId : Int? = null

    constructor() : this("", "", null, -1f, -1)
}