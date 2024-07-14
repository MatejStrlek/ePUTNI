package hr.algebra.eputni

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import hr.algebra.eputni.dao.FirestoreUserLogin
import hr.algebra.eputni.dao.UserRepository
import hr.algebra.eputni.databinding.ActivityLoginBinding
import hr.algebra.eputni.framework.startActivity

@Suppress("DEPRECATION")
class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding

    private val auth = FirebaseAuth.getInstance()
    private var oneTapClient: SignInClient? = null
    private lateinit var signInRequest: BeginSignInRequest
    private val userRepository: UserRepository = FirestoreUserLogin()

    companion object {
        private const val REQ_ONE_TAP = 2
        private const val TAG = "LoginActivity"
        private const val webClientID = BuildConfig.webClientId
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        overridePendingTransition(
            R.anim.fade_in,
            R.anim.fade_out
        )

        initLogin()
    }

    private fun initLogin() {
        oneTapClient = Identity.getSignInClient(this)
        signInRequest = BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    .setServerClientId(webClientID)
                    .setFilterByAuthorizedAccounts(false)
                    .build()
            )
            .build()

        binding.btnSignIn.setOnClickListener {
            Log.d(TAG, "Sign in clicked")
            signIn()
        }
    }

    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        updateInfo(currentUser)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQ_ONE_TAP) {
            if (resultCode == RESULT_OK && data != null) {
                try {
                    val credential = oneTapClient?.getSignInCredentialFromIntent(data)
                    val idToken = credential?.googleIdToken
                    if (idToken != null) {
                        firebaseAuthWithGoogle(idToken)
                    } else {
                        Log.d(TAG, "No ID token!")
                    }
                } catch (e: ApiException) {
                    Log.w(TAG, "Sign-in failed", e)
                    when (e.statusCode) {
                        CommonStatusCodes.CANCELED -> {
                            Log.d("one tap", "One-tap dialog was closed.")
                            Snackbar.make(
                                binding.root,
                                "One-tap dialog was closed.",
                                Snackbar.LENGTH_INDEFINITE
                            ).show()
                        }

                        CommonStatusCodes.NETWORK_ERROR -> {
                            Log.d("one tap", "One-tap encountered a network error.")
                            Snackbar.make(
                                binding.root,
                                "One-tap encountered a network error.",
                                Snackbar.LENGTH_INDEFINITE
                            ).show()
                        }

                        else -> {
                            Log.d(
                                "one tap", "Couldn't get credential from result." +
                                        " (${e.localizedMessage})"
                            )
                            Snackbar.make(
                                binding.root, "Couldn't get credential from result.\" +\n" +
                                        " (${e.localizedMessage})", Snackbar.LENGTH_INDEFINITE
                            ).show()
                        }
                    }
                }
            } else {
                Log.w(TAG, "Sign-in canceled or failed. resultCode: $resultCode")
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null && user.email != null && user.email!!.endsWith(getString(R.string.allowed_domain))) {
                        Log.d(TAG, "signInWithCredential:success")
                        checkAndSaveUser(user)
                    } else {
                        Log.w(TAG, "Sign-in failed: unauthorized domain")
                        Toast.makeText(
                            this,
                            getString(R.string.unauthorized_domain), Toast.LENGTH_SHORT
                        ).show()
                        signOut()
                    }
                } else {
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    Toast.makeText(
                        this,
                        getString(R.string.authentication_failed), Toast.LENGTH_SHORT
                    ).show()
                    updateInfo(null)
                }
            }
    }

    private fun checkAndSaveUser(user: FirebaseUser) {
        userRepository.userExists(user.email!!,
            { exists ->
                if (exists) {
                    Log.d(TAG, "User exists: ${user.displayName} (${user.email})")
                    startActivity<MainActivity>()
                    finish()
                } else {
                    saveUserInFirestore(user)
                }
            },
            {
                Log.w(TAG, "Error checking user data", it)
                Toast.makeText(
                    this,
                    getString(R.string.failed_to_check_user_data), Toast.LENGTH_SHORT
                ).show()
                signOut()
            })
    }

    private fun saveUserInFirestore(user: FirebaseUser) {
        userRepository.saveUser(user,
            {
                startActivity<MainActivity>()
                finish()
            },
            {
                Log.w(TAG, "Error saving user data", it)
                Toast.makeText(
                    this,
                    getString(R.string.failed_to_save_user_data), Toast.LENGTH_SHORT
                ).show()
                signOut()
            })
    }

    private fun signOut() {
        auth.signOut()
        updateInfo(null)
    }

    private fun signIn() {
        oneTapClient?.beginSignIn(signInRequest)
            ?.addOnSuccessListener(this) { result ->
                try {
                    startIntentSenderForResult(
                        result.pendingIntent.intentSender, REQ_ONE_TAP,
                        null, 0, 0, 0, null
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Couldn't start One Tap UI: ${e.localizedMessage}")
                }
            }
            ?.addOnFailureListener(this) { e ->
                Log.e(TAG, "Error in beginSignIn", e)
            }
    }

    private fun updateInfo(user: FirebaseUser?) {
        if (user != null) {
            Log.d(TAG, "User signed in: ${user.displayName} (${user.email})")
            Toast.makeText(this, "Pozdrav, ${user.displayName}!", Toast.LENGTH_SHORT).show()
            startActivity<MainActivity>()
            finish()
        } else {
            Log.d(TAG, "User not signed in")
        }
    }
}