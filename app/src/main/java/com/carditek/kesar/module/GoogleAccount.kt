package com.carditek.kesar.module

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.carditek.kesar.Account
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

class GoogleAccount(private val application: Context) : Account {
    private var google: GoogleSignInAccount? = null
    private val _name = MutableLiveData<String>()
    private val _email = MutableLiveData<String>()
    private val _photo = MutableLiveData<Uri>()

    override val name: LiveData<String> = _name
    override val email: LiveData<String> = _email
    override val photo: LiveData<Uri> = _photo

    override val token: String?
        get() = google?.idToken

    override fun maybeSignIn(activity: Activity, signInLauncher: ActivityResultLauncher<Intent>) {
        if (GoogleSignIn.getLastSignedInAccount(application) != null) return
        signInLauncher.launch(client(activity).signInIntent)
    }

    override fun signOut(activity: Activity) {
        client(activity).signOut()
        set(null)
    }

    override fun onSignInSuccess(data: Intent?) {
        val task = GoogleSignIn.getSignedInAccountFromIntent(data)
        try {
            set(task.getResult(ApiException::class.java)!!)
        } catch (e: ApiException) {
            Log.w(TAG, "Failed to authenticate", task.exception)
            set(null)
        }
    }

    override fun refresh() {
        super.refresh()
        google?.let {
            if (it.isExpired) {
                Log.i(TAG, "Account expired, refreshing tokens")
                val client = GoogleSignIn.getClient(application, options())
                client.silentSignIn().addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        set(task.result)
                    }
                }
            }
        }
    }

    init {
        set(GoogleSignIn.getLastSignedInAccount(application))
    }

    private fun set(google: GoogleSignInAccount?) {
        this.google = google
        if (google != null) {
            google.displayName?.let { _name.postValue(it) }
            google.email?.let {
                _email.postValue(it)
                Log.i(TAG, "--------------------Logged in: ${_name.value} <$it>")
                firebase.setCustomKey("E-MAIL", it)
            }
            google.photoUrl?.let { _photo.postValue(it) }
        } else {
            firebase.setCustomKey("E-MAIL", "")
            Log.i(TAG, "Logged out")
        }
    }

    private fun client(activity: Activity): GoogleSignInClient {
        val options = options()
        return GoogleSignIn.getClient(activity, options)
    }

    private fun options() = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(OAUTH_CLIENT_ID)
        .requestEmail()
        .build()
    companion object {
        private val firebase = FirebaseCrashlytics.getInstance()
        private const val OAUTH_CLIENT_ID =
            "7770627481-6215hqbpcg9hm1lku8ni32pod4fngnoa.apps.googleusercontent.com"
            //"661728900475-228rakg81k48a98pmsgqvk2dfrjd3bc6.apps.googleusercontent.com"
        //http://661728900475-228rakg81k48a98pmsgqvk2dfrjd3bc6.apps.googleusercontent.com  orignal
        private const val TAG = "Account"
    }
}

@Module
@InstallIn(SingletonComponent::class)
object AccountModule {
    @Provides
    @Singleton
    fun provideAccount(@ApplicationContext context: Context): Account = GoogleAccount(context)
}
