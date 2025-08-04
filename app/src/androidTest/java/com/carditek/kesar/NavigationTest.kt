package com.carditek.kesar

import android.view.View
import android.widget.TextView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.DrawerActions.open
import androidx.test.espresso.contrib.DrawerMatchers.isClosed
import androidx.test.espresso.contrib.DrawerMatchers.isOpen
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.hamcrest.Matcher
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.instanceOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

@HiltAndroidTest
class NavigationTest {
    @get:Rule(order = 0)
    var hilt = HiltAndroidRule(this)

    @get:Rule(order = 1)
    var activity: ActivityScenarioRule<MainActivity> =
        ActivityScenarioRule(MainActivity::class.java)

    @Inject
    lateinit var account: Account

    @Before
    fun init() {
        hilt.inject()
    }

    private fun navigateAndCheckTitle(id: Int, title: String) {
        openDrawer()
        onView(withId(id))
            .perform(click())
        onView(allOf(instanceOf(TextView::class.java), withParent(withId(R.id.toolbar))))
            .check(matches(withText(title)))
    }

    private fun openDrawer() {
        onView(withId(R.id.drawer_layout))
            .check(matches(isClosed()))
            .perform(open())
            .check(matches(isOpen()))
    }

    @Test
    fun checkNavigateToRecord() {
        navigateAndCheckTitle(R.id.nav_record, "Record")
    }

    @Test
    fun checkNavigateToStatus() {
        navigateAndCheckTitle(R.id.nav_status, "Status")
    }

    @Test
    fun checkAccountIsDisplayed() {
        openDrawer()
        // TODO(vjn): sleeping [sic] for data binding callbacks.  Replace with idling resource.
        onView(withId(R.id.account_email))
            .perform(object: ViewAction {
                override fun getConstraints(): Matcher<View> {
                    return isEnabled()
                }

                override fun getDescription(): String {
                    return ""
                }

                override fun perform(uiController: UiController?, view: View?) {
                    uiController?.loopMainThreadForAtLeast(10)
                }
            })
            .check(matches(withText(account.email.value)))
        onView(withId(R.id.account_name))
            .check(matches(withText(account.name.value)))
    }
}
