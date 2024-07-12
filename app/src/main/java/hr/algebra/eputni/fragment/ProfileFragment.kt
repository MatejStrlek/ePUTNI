package hr.algebra.eputni.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import hr.algebra.eputni.LoginActivity
import hr.algebra.eputni.MainActivity
import hr.algebra.eputni.R
import hr.algebra.eputni.dao.FirestoreUserLogin
import hr.algebra.eputni.dao.UserRepository
import hr.algebra.eputni.databinding.FragmentProfileBinding

class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val userRepository: UserRepository = FirestoreUserLogin()
    private val auth = FirebaseAuth.getInstance()

    private var originalRole: String? = null

    companion object {
        private const val TAG = "ProfileFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnSignOut.setOnClickListener {
            signOut()
        }

        binding.etRole.addTextChangedListener {
            validateRoleField()
        }

        initUser()
    }

    override fun onPause() {
        super.onPause()
        val currentRole = binding.etRole.text.toString()
        if (currentRole != originalRole) {
            updateUserRole(currentRole)
        }
    }

    private fun updateUserRole(role: String) {
        val userId = auth.currentUser?.uid
        if (userId != null && role.isNotEmpty()) {
            userRepository.updateUserRole(userId, role,
                onSuccess = {
                    Toast.makeText(context, getString(R.string.role_saved), Toast.LENGTH_SHORT).show()
                    originalRole = role
                },
                onFailure = {
                    Toast.makeText(context, getString(R.string.role_failed_save), Toast.LENGTH_SHORT).show()
                })
        }
    }

    private fun initUser() {
        val userId = auth.currentUser!!.uid
        userRepository.getUser(
            userId,
            { user ->
                binding.tvName.text = user.displayName
                binding.tvEmail.text = user.email
                if (user.photoUrl != null) {
                    Glide.with(this)
                        .load(user.photoUrl)
                        .circleCrop()
                        .into(binding.ivAvatar)
                }
                fetchUserRole(userId)
            },
            { e ->
                Log.e(TAG, "Failed to get user", e)
            }
        )
    }

    private fun fetchUserRole(userId: String) {
        userRepository.getUserRole(
            userId,
            { role ->
                originalRole = role
                binding.etRole.setText(role)
                validateRoleField()
            },
            { e ->
                Log.e(TAG, "Failed to get user role", e)
            }
        )
    }

    private fun validateRoleField() {
        val role = binding.etRole.text.toString()
        val isValid = role.isNotEmpty()

        (activity as? MainActivity)?.setNavigationEnabled(isValid)
    }

    private fun signOut() {
        auth.signOut()
        Log.d(TAG, "User signed out")
        startActivity(Intent(activity, LoginActivity::class.java))
        activity?.finish()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}