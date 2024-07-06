package hr.algebra.eputni.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import hr.algebra.eputni.LoginActivity
import hr.algebra.eputni.dao.FirestoreUserLogin
import hr.algebra.eputni.dao.UserRepository
import hr.algebra.eputni.databinding.FragmentProfileBinding

class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val userRepository: UserRepository = FirestoreUserLogin()
    private val auth = FirebaseAuth.getInstance()

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
        startActivity(Intent(activity, LoginActivity::class.java))
        activity?.finish()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}