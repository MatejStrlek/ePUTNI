package hr.algebra.eputni

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import hr.algebra.eputni.databinding.ActivitySpashScreenBinding

class SpashScreenActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySpashScreenBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySpashScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        startAnimations()
    }

    private fun startAnimations() {

    }
}