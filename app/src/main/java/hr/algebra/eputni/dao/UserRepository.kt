package hr.algebra.eputni.dao

import com.google.firebase.auth.FirebaseUser

interface UserRepository {
    fun saveUser(user: FirebaseUser, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)
    fun getUser(uid: String, onSuccess: (FirebaseUser) -> Unit, onFailure: (Exception) -> Unit)
    fun userExists(email: String, onSuccess: (Boolean) -> Unit, onFailure: (Exception) -> Unit)
    fun updateUserRole(uid: String, role: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)
    fun getUserRole(uid: String, onSuccess: (String) -> Unit, onFailure: (Exception) -> Unit)
}