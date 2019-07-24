package com.tech.ssylix.lyght_reader.logic.utitlities

import android.app.Activity
import com.firebase.ui.auth.AuthUI

class Auth {
    

    fun beginUserLogin(activity: Activity, INT_TAG: Int) {
        activity.startActivityForResult(
            AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(
                    arrayListOf(
                        AuthUI.IdpConfig.GoogleBuilder().build(),
                        AuthUI.IdpConfig.EmailBuilder().build()
                    )
                )
                //.setIsSmartLockEnabled(true)
                .build(), INT_TAG
        )
    }

    companion object {
        const val UID_ARG = "uid"
    }
}