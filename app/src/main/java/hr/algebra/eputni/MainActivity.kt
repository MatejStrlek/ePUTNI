package hr.algebra.eputni

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import hr.algebra.eputni.dao.FirestoreUserLogin
import hr.algebra.eputni.dao.UserRepository
import hr.algebra.eputni.databinding.ActivityMainBinding
import hr.algebra.eputni.framework.startActivity

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private val userRepository: UserRepository = FirestoreUserLogin()
    private val auth = FirebaseAuth.getInstance()

    companion object {
        private const val TAG = "MainActivity"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnSignOut.setOnClickListener {
            signOut()
        }

        initUser()
    }

    private fun initUser() {
        userRepository.getUser(
            auth.currentUser!!.uid,
            { user ->
                binding.tvName.text = user.displayName
                binding.tvEmail.text = user.email
                if (user.photoUrl != null) {
                    Glide.with(this)
                        .load(user.photoUrl)
                        .circleCrop()
                        .into(binding.ivAvatar)
                }
            },
            { e ->
                Log.e(TAG, "Failed to get user", e)
            }
        )
    }

    private fun signOut() {
        auth.signOut()
        Log.d(TAG, "User signed out")
        startActivity<LoginActivity>()
        finish()
    }
}