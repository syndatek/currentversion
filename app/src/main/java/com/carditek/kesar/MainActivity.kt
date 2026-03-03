package com.carditek.kesar

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
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
import javax.inject.Inject

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
    lateinit var appCache: Cache

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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

        navigation.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.nav_sign_out -> {
                    account.signOut(this)
                    account.maybeSignIn(this, RC_SIGN_IN)
                }
                R.id.nav_device_list -> {
                    BluetoothUtils(this, device).selectPatch()
                }
                R.id.nav_snr_saturation -> {
                    showLeadSelectionDialog()
                }
                else -> {
                    NavigationUI.onNavDestinationSelected(it, navController)
                }
            }

            drawer.closeDrawers()
            false
        }

        Controller.ensure(this)
    }

    override fun onStart() {
        super.onStart()
        account.maybeSignIn(this, RC_SIGN_IN)
    }

    override fun onActivityResult(code: Int, result: Int, data: Intent?) {
        super.onActivityResult(code, result, data)
        if (code == RC_SIGN_IN) {
            if (result == Activity.RESULT_OK) {
                account.onSignInSuccess(data)
            } else {
                account.signOut(this)
            }
        } else if (code == RC_ENABLE_BLUETOOTH) {
            if (result != Activity.RESULT_OK) {
                Log.w(TAG, "Failed to enable bluetooth: canceled")
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    private fun fixHeaderBinding(navigation: NavigationView) {
        // Remove existing header, and inflate a new one into place to get binding right.
        if (navigation.headerCount > 0)
            navigation.removeHeaderView(navigation.getHeaderView(0))
        val headerBinding = NavHeaderMainBinding.inflate(layoutInflater, navigation, false)
        headerBinding.account = account
        navigation.addHeaderView(headerBinding.root)
    }

    private fun showLeadSelectionDialog() {
        val currentSelectedLeads = appCache.getSelectedLeadsForSNRAndSaturation()
        val dialog = com.carditek.kesar.util.filters.edgecomputing.LeadSelectionDialog(
            initialSelectedLeads = currentSelectedLeads,
            onLeadsSelected = { selectedLeads ->
                appCache.setSelectedLeadsForSNRAndSaturation(selectedLeads)
                android.widget.Toast.makeText(
                    this,
                    "SNR & Saturation calculation updated for ${selectedLeads.size} lead(s)",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            }
        )
        dialog.show(supportFragmentManager, "LeadSelectionDialog")
    }

    companion object {
        private const val RC_SIGN_IN = 9001
        private const val RC_ENABLE_BLUETOOTH = 9002
        private const val TAG = "MainActivity"
    }
}
