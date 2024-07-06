package hr.algebra.eputni.dao

import com.google.firebase.auth.FirebaseUser

interface UserRepository {
    fun saveUser(user: FirebaseUser, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)
}