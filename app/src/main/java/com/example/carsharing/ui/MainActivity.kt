package com.example.carsharing.ui

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.carsharing.R
import com.example.carsharing.ui.dashboard.DashboardController
import com.example.carsharing.ui.login.LoginActivity
import com.example.carsharing.ui.payment_methods.PaymentMethodsRecyclerViewAdapter
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var auth: FirebaseAuth
    private lateinit var controller: NavController
    private lateinit var toolbar: Toolbar
    private lateinit var doneIV: ImageView
    private lateinit var viewModel: MainActivityViewModel
    private lateinit var drawer: DrawerLayout
    private lateinit var drawerToggle: ActionBarDrawerToggle
    private var editModeListener: EditModeListener? = null
    private var lastFragment: Fragment? = null
    private var lastFragmentManager: FragmentManager? = null
    private var currentState = MainActivityState.NOT_EDITING
    private var dots: MenuItem? = null

    private val listener = NavController.OnDestinationChangedListener { controller, _, _ ->
        Log.d(TAG, "Current dest ${controller.currentDestination!!.label}  $dots")

        dots?.isVisible =
            controller.currentDestination!!.label == getString(R.string.payment_menthods)
        doneIV.visibility = INVISIBLE
        editModeListener?.setEditMode(PaymentMethodsRecyclerViewAdapter.EditMode.OFF)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate")
        setContentView(R.layout.activity_main)
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        viewModel = ViewModelProvider(this).get(MainActivityViewModel::class.java)

        doneIV = toolbar.findViewById(R.id.doneIV)
        doneIV.setOnClickListener {
            changeIcon()
        }

        auth = FirebaseAuth.getInstance()

        drawer = findViewById(R.id.drawer_layout)
        drawerToggle = setupDrawerToggle()

        drawerToggle.isDrawerIndicatorEnabled = true
        drawerToggle.syncState()
        val navView: NavigationView = findViewById(R.id.nav_view)
        controller = findNavController(R.id.nav_host_fragment)
        val headerLayout = navView.getHeaderView(0)
        val emailTV = headerLayout.findViewById<TextView>(R.id.myEmailTV)
        emailTV.text = auth.currentUser!!.email
        val nameTV = headerLayout.findViewById<TextView>(R.id.nameTV)
        viewModel.getFullName().observe(this, {
            nameTV.text = it
        })

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_dashboard, R.id.nav_history_of_rentals, R.id.nav_change_email,
                R.id.nav_change_password, R.id.nav_payment_methods
            ), drawer
        )
        setupActionBarWithNavController(controller, appBarConfiguration)
        navView.setupWithNavController(controller)

        val signOutButton = navView.findViewById<Button>(R.id.signOutButton)
        signOutButton.setOnClickListener {
            auth.signOut()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            finish()
        }

        drawer.addDrawerListener(drawerToggle)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        dots = menu.findItem(R.id.action_settings)
        dots!!.isVisible = false

        dots!!.setOnMenuItemClickListener {
            changeIcon()
            return@setOnMenuItemClickListener true
        }

        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onResume() {
        super.onResume()
        controller.addOnDestinationChangedListener(listener)
    }

    override fun onPause() {
        controller.removeOnDestinationChangedListener(listener)
        super.onPause()
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        drawerToggle.syncState()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        drawerToggle.onConfigurationChanged(newConfig)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        if (this.lastFragment != null) {
            val transaction = lastFragmentManager!!.beginTransaction()
            transaction.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right)
            transaction.replace(
                R.id.nav_host_fragment,
                this.lastFragment!!
            )
            transaction.addToBackStack(null)
            transaction.commit()
            this.lastFragment = null
            lastFragmentManager = null
        } else {
            super.onBackPressed()
        }
    }

    private fun setupDrawerToggle(): ActionBarDrawerToggle {
        return ActionBarDrawerToggle(
            this,
            drawer,
            toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
    }

    private fun changeIcon() {
        when (currentState) {
            MainActivityState.EDITING -> {
                editModeListener?.setEditMode(PaymentMethodsRecyclerViewAdapter.EditMode.OFF)
                currentState = MainActivityState.NOT_EDITING
                dots!!.isVisible = true
                doneIV.visibility = INVISIBLE
            }
            MainActivityState.NOT_EDITING -> {
                editModeListener?.setEditMode(PaymentMethodsRecyclerViewAdapter.EditMode.ON)
                currentState = MainActivityState.EDITING
                dots!!.isVisible = false
                doneIV.visibility = VISIBLE
            }
        }
    }

    fun updateApi(editModeListener: EditModeListener) {
        this.editModeListener = editModeListener
    }

    fun updateBackButtonAction(fragment: Fragment, fragmentManager: FragmentManager) {
        lastFragment = fragment
        lastFragmentManager = fragmentManager
    }

    private enum class MainActivityState {
        EDITING,
        NOT_EDITING
    }

    interface EditModeListener {
        fun setEditMode(editMode: PaymentMethodsRecyclerViewAdapter.EditMode)
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}