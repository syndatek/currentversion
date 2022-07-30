package com.carditek.kesar

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.LiveData

interface Account {
    val name: LiveData<String>
    val email: LiveData<String>
    val photo: LiveData<Uri>
    val token: String?

    fun maybeSignIn(activity: Activity, code: Int) {}
    fun signOut(activity: Activity) {}
    fun onSignInSuccess(data: Intent?) {}
    fun refresh() {}
}
