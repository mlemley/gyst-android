package app.gyst

import android.os.Bundle
import android.view.LayoutInflater
import androidx.annotation.VisibleForTesting
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.Navigation
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import app.gyst.common.gone
import app.gyst.common.show
import app.gyst.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    lateinit var binder: ActivityMainBinding

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    val topLevelDestinationIds =
        setOf(R.id.nav_onboarding_create_account, R.id.nav_introduction_screen, R.id.nav_login_screen, R.id.nav_financial_overview)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binder = ActivityMainBinding.inflate(LayoutInflater.from(this))
        setContentView(binder.root)
        setSupportActionBar(binder.toolbar)
        val navController = Navigation.findNavController(this, R.id.nav_host_fragment)
        val appBarConfiguration = AppBarConfiguration(topLevelDestinationIds)
        setupActionBarWithNavController(navController, appBarConfiguration)
        navController.addOnDestinationChangedListener { controller, destination, arguments ->
            when (destination.id) {
                R.id.nav_splash_screen -> binder.appbar.gone()
                else -> binder.appbar.show()
            }

        }
    }
}