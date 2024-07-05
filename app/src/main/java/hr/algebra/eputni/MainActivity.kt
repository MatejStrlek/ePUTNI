package hr.algebra.eputni

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.core.net.toUri
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import hr.algebra.eputni.databinding.ActivityMainBinding
import hr.algebra.eputni.framework.startActivity

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private lateinit var auth: FirebaseAuth

    companion object {
        private const val TAG = "MainActivity"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        binding.btnSignOut.setOnClickListener {
            signOut()
        }

        initUser()
    }

    private fun initUser() {
        val user = intent.getStringExtra("user")
        val email = intent.getStringExtra("email")
        val photo = intent.getStringExtra("photo")

        binding.tvName.text = user
        binding.tvEmail.text = email
        if (photo != null) {
            Glide.with(this)
                .load(photo.toUri())
                .circleCrop()
                .into(binding.ivAvatar)
        }
    }

    private fun signOut() {
        auth.signOut()
        Log.d(TAG, "User signed out")
        startActivity<LoginActivity>()
        finish()
    }
}