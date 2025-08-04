package com.carditek.kesar

import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

@HiltAndroidTest
class AccountTest {
    @get:Rule
    var hilt = HiltAndroidRule(this)

    @Inject
    lateinit var account: Account

    @Before
    fun init() {
        hilt.inject()
    }

    @Test
    fun checkEmail() {
        assertEquals("test@carditek.com", account.email.value)
        assertEquals("Sydäntek Tester", account.name.value)
    }
}
