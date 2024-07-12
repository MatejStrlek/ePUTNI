package hr.algebra.eputni

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import hr.algebra.eputni.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private var isNavigationEnabled: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        val bottomNavView = binding.bottomNavView
        NavigationUI.setupWithNavController(bottomNavView, navController)

        bottomNavView.setOnItemSelectedListener {
            if (isNavigationEnabled) {
                NavigationUI.onNavDestinationSelected(it, navController)
                true
            }
            else {
                Toast.makeText(this, R.string.role_required, Toast.LENGTH_SHORT).show()
                false
            }
        }
    }

    fun setNavigationEnabled(enabled: Boolean) {
        isNavigationEnabled = enabled
    }
}