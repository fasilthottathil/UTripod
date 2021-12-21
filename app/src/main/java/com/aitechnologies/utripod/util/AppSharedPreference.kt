package com.aitechnologies.utripod.util

import android.content.Context
import android.content.SharedPreferences
import com.aitechnologies.utripod.util.Constants.IS_LOGIN
import com.aitechnologies.utripod.util.Constants.MY_PREFERENCE
import com.aitechnologies.utripod.util.Constants.PREFERENCE_MODE
import com.aitechnologies.utripod.util.Constants.PROFESSION
import com.aitechnologies.utripod.util.Constants.PROFILE_URL
import com.aitechnologies.utripod.util.Constants.USERNAME
import com.aitechnologies.utripod.util.Constants.USER_ID

class AppSharedPreference {
    companion object {

        private fun Context.getSharedPreference(): SharedPreferences {
            return getSharedPreferences(MY_PREFERENCE, PREFERENCE_MODE)
        }

        fun Context.isLogin(): Boolean {
            return getSharedPreference().getBoolean(IS_LOGIN, false)
        }

        fun Context.login(
            username: String,
            userId: String,
            profileUrl: String,
            profession:String
        ) {
            getSharedPreference().edit().apply {
                putString(USERNAME, username)
                putString(USER_ID, userId)
                putString(PROFILE_URL, profileUrl)
                putString(PROFESSION,profession)
                putBoolean(IS_LOGIN, true)
            }.apply()
        }

        fun Context.logout() {
            getSharedPreference().edit().apply {
                putBoolean(IS_LOGIN, false)
            }.apply()
        }

        fun Context.getUsername(): String {
            return getSharedPreference().getString(USERNAME, "").toString()
        }

        fun Context.getProfileUrl(): String {
            return getSharedPreference().getString(PROFILE_URL, "").toString()
        }

        fun Context.updateProfileUrl(profileUrl: String) {
            getSharedPreference().edit().apply {
                putString(PROFILE_URL, profileUrl)
            }.apply()
        }

        fun Context.getProfession(): String {
            return getSharedPreference().getString(PROFESSION, "").toString()
        }


    }
}