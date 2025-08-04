package com.carditek.kesar

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.carditek.kesar.module.AccountModule
import dagger.Module
import dagger.Provides
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Singleton

class FakeAccount : Account {
    private val _name = MutableLiveData<String>("Sydäntek Tester")
    private val _email = MutableLiveData<String>("test@carditek.com")
    private val _photo = MutableLiveData<Uri>(Uri.parse(
        "https://ssl.gstatic.com/docs/common/profile/chinchilla_lg.png"))
    override val name: LiveData<String> = _name
    override val email: LiveData<String> = _email
    override val photo: LiveData<Uri> = _photo
    override val token: String = "*invalid*"
}

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [AccountModule::class]
)
object FakeAccountModule {
    @Provides
    @Singleton
    fun provideAccount(@ApplicationContext context: Context): Account = FakeAccount()
}
