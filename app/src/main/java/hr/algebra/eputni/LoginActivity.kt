package hr.algebra.eputni

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.ComponentActivity
import hr.algebra.eputni.databinding.ActivityLoginBinding

@Suppress("DEPRECATION")
class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= 34) {
            overrideActivityTransition(
                ComponentActivity.OVERRIDE_TRANSITION_OPEN,
                R.anim.fade_in,
                R.anim.fade_out
            )
        } else overridePendingTransition(
            R.anim.fade_in,
            R.anim.fade_out
        )

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initLogin()
    }

    private fun initLogin() {

    }
}