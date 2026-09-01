package com.carditek.kesar

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.annotation.IdRes
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.*
import com.carditek.kesar.bluetooth.State
import com.carditek.kesar.databinding.ActivityMainBinding
import com.carditek.kesar.databinding.NavHeaderMainBinding
import com.carditek.kesar.service.Controller
import com.carditek.kesar.util.BluetoothUtils
import com.google.android.material.navigation.NavigationView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    @Inject
    lateinit var account: Account

    @Inject
    lateinit var device: Device

    @Inject
    lateinit var state: State

    @Inject
    lateinit var medicalHistoryUploadNotifier: MedicalHistoryUploadNotifier

    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            account.onSignInSuccess(result.data)
        } else {
            account.signOut(this)
        }
    }

    private val enableBluetoothLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode != Activity.RESULT_OK) {
            Log.w(TAG, "Failed to enable bluetooth: canceled")
        }
    }

    private val requestPostNotificationsPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        Controller.ensure(this@MainActivity)
    }


    override fun onCreate(savedInstanceState: Bundle?) {





        super.onCreate(savedInstanceState)
        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(this))
            Log.d("PYTHON", "Chaquopy started from MainActivity")
        }

        val needsPostNotificationRequest = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED

        if (needsPostNotificationRequest) {
            requestPostNotificationsPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.appBar.toolbar)

        val drawer = binding.drawerLayout
        val navigation = binding.navView

        fixHeaderBinding(navigation)  // to get data binding right for the header.

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_record, R.id.nav_status,
                R.id.nav_live, R.id.nav_settings
            ), drawer
        )
        val navHostFragment = (supportFragmentManager.findFragmentById(R.id.nav_host_fragment)
                as NavHostFragment)
        val navController = navHostFragment.navController
        setupActionBarWithNavController(navController, appBarConfiguration)
        navigation.setupWithNavController(navController)

        // Custom handling: top-level screens must always switch when chosen (e.g. after
        // StatusFragment navigates to Live via Start, the back stack is [Status, Live];
        // choosing Status in the drawer must pop back and show Status — use popUpTo start).
        navigation.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_sign_out -> {
                    account.signOut(this)
                    account.maybeSignIn(this, googleSignInLauncher)
                    drawer.closeDrawers()
                    false
                }
                R.id.nav_device_list -> {
                    BluetoothUtils(this, device, enableBluetoothLauncher).selectPatch()
                    drawer.closeDrawers()
                    false
                }
                R.id.nav_record, R.id.nav_status, R.id.nav_live, R.id.nav_settings -> {
                    val handled = navigateDrawerTopLevel(navController, item.itemId)
                    if (handled) {
                        drawer.closeDrawers()
                    }
                    handled
                }
                else -> {
                    val handled = NavigationUI.onNavDestinationSelected(item, navController)
                    if (handled) {
                        drawer.closeDrawers()
                    }
                    handled
                }
            }
        }

        if (!needsPostNotificationRequest) {
            Controller.ensure(this)
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                medicalHistoryUploadNotifier.medicalHistorySavedEvents.collect {
                    Toast.makeText(
                        this@MainActivity,
                        getString(R.string.history_saved_success),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        account.maybeSignIn(this, googleSignInLauncher)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    /**
     * Navigate between top-level drawer destinations reliably: pop back to [NavController.graph]
     * start destination first, then navigate. Fixes stacks like [nav_status, nav_live] where
     * tapping Status in the menu must return to Status.
     */
    private fun navigateDrawerTopLevel(navController: NavController, @IdRes destId: Int): Boolean {
        val graph = navController.graph
        if (graph.findNode(destId) == null) return false
        val options = NavOptions.Builder()
            .setLaunchSingleTop(true)
            .setRestoreState(true)
            .setPopUpTo(graph.startDestinationId, false)
            .build()
        return try {
            navController.navigate(destId, null, options)
            true
        } catch (e: IllegalArgumentException) {
            Log.w(TAG, "navigateDrawerTopLevel failed for $destId", e)
            false
        }
    }

    private fun fixHeaderBinding(navigation: NavigationView) {
        // Remove existing header, and inflate a new one into place to get binding right.
        if (navigation.headerCount > 0)
            navigation.removeHeaderView(navigation.getHeaderView(0))
        val headerBinding = NavHeaderMainBinding.inflate(layoutInflater, navigation, false)
        headerBinding.account = account
        navigation.addHeaderView(headerBinding.root)
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}
