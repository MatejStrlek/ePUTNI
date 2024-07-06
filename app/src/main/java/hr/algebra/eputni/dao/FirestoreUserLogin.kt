package hr.algebra.eputni.dao

import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

class FirestoreUserLogin : UserRepository {

    private val db = FirebaseFirestore.getInstance()

    override fun saveUser(user: FirebaseUser, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val userData = hashMapOf(
            "displayName" to user.displayName,
            "email" to user.email,
            "photoUrl" to user.photoUrl.toString()
        )

        db.collection("users").document(user.uid)
            .set(userData)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { e ->
                onFailure(e)
            }
    }
}