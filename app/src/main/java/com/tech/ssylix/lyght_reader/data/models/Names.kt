package com.tech.ssylix.lyght_reader.data.models

import androidx.annotation.Keep

@Keep
data class Names(
    var firstName: String,
    var middleName: String,
    var surname: String,
    var userName: String,
    var incognitoTag: String
) { constructor() : this("", "", "", "", "") }
