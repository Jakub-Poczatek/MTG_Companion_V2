package org.wit.mtgcompanionv2.ui.home

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.navigation.ui.AppBarConfiguration
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.firebase.auth.FirebaseUser
import org.wit.mtgcompanionv2.R
import org.wit.mtgcompanionv2.databinding.HomeBinding
import org.wit.mtgcompanionv2.databinding.NavHeaderBinding
import org.wit.mtgcompanionv2.firebase.FirebaseImageManager
import org.wit.mtgcompanionv2.ui.auth.LoggedInViewModel
import org.wit.mtgcompanionv2.ui.auth.Login
import timber.log.Timber

class Home : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var binding: HomeBinding
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var navHeaderBinding: NavHeaderBinding
    private lateinit var loggedInViewModel: LoggedInViewModel
    private lateinit var headerView : View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = HomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        drawerLayout = binding.drawerLayout
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        val navHostFragment = supportFragmentManager.
        findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        appBarConfiguration = AppBarConfiguration(setOf(
            R.id.cardFragment, R.id.cardListFragment, R.id.mapFragment, R.id.loginFragment), drawerLayout)
        setupActionBarWithNavController(navController, appBarConfiguration)

        val navView = binding.navView
        navView.setupWithNavController(navController)

        initNavHeader()
    }

    public override fun onStart() {
        super.onStart()
        loggedInViewModel = ViewModelProvider(this).get(LoggedInViewModel::class.java)
        loggedInViewModel.liveFirebaseUser.observe(this, Observer { firebaseUser ->
            if (firebaseUser != null)
                updateNavHeader(firebaseUser)
        })
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    private fun updateNavHeader(currentUser: FirebaseUser) {
        FirebaseImageManager.userImageUri.observe(this) { result ->
            if (result == Uri.EMPTY) {
                Timber.i("MTGComp NO Existing imageUri")
                if (currentUser.photoUrl != null) {
                    //if you're a google user
                    FirebaseImageManager.updateUserImage(
                        currentUser.uid,
                        currentUser.photoUrl,
                        navHeaderBinding.navHeaderImage,
                        false
                    )
                } else {
                    Timber.i("MTGComp Loading Existing Default imageUri")
                    FirebaseImageManager.updateDefaultImage(
                        currentUser.uid,
                        R.drawable.ic_launcher_background,
                        navHeaderBinding.navHeaderImage
                    )
                }        } else // load existing image from firebase
            {
                Timber.i("MTGComp Loading Existing imageUri")
                FirebaseImageManager.updateUserImage(
                    currentUser.uid,
                    FirebaseImageManager.userImageUri.value,
                    navHeaderBinding.navHeaderImage, false
                )
            }    }
        navHeaderBinding.navHeaderEmail.text = currentUser.email
        if(currentUser.displayName != null)
            navHeaderBinding.navHeaderName.text = currentUser.displayName
    }


    private fun initNavHeader() {
        Timber.i("MTGComp Init Nav Header")
        headerView = binding.navView.getHeaderView(0)
        navHeaderBinding = NavHeaderBinding.bind(headerView)
    }


    fun signOut(item: MenuItem) {
        loggedInViewModel.LogOut()
        //Launch Login activity and clear the back stack to stop navigating back to the Home activity
        val intent = Intent(this, Login::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }
}