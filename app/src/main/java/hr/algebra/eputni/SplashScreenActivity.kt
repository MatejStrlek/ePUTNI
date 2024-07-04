package hr.algebra.eputni

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import hr.algebra.eputni.databinding.ActivitySplashScreenBinding
import hr.algebra.eputni.framework.applyAnimation
import hr.algebra.eputni.framework.callDelayed
import hr.algebra.eputni.framework.isOnline
import hr.algebra.eputni.framework.startActivity

private const val DELAY = 3500L
@SuppressLint("CustomSplashScreen")
class SplashScreenActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySplashScreenBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        startAnimations()
        redirect()
    }

    private fun redirect() {
        if (isOnline()) {
            callDelayed(DELAY) { startActivity<LoginActivity>() }
        } else {
            binding.tvError.text = getString(R.string.no_internet)
            callDelayed(DELAY) { finish() }
        }
    }

    private fun startAnimations() {
        binding.ivSplash.applyAnimation(R.anim.left_to_right)
        binding.tvSplash.applyAnimation(R.anim.blink)
    }
}