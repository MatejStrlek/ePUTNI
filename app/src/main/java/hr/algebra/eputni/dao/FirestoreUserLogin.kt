package hr.algebra.eputni.dao

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

class FirestoreUserLogin : UserRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun saveUser(
        user: FirebaseUser,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit) {
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

    override fun getUser(
        uid: String,
        onSuccess: (FirebaseUser) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val user = auth.currentUser
        if (user != null && user.uid == uid) {
            db.collection("users").document(uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        onSuccess(user)
                    } else {
                        onFailure(Exception("User not found"))
                    }
                }
                .addOnFailureListener { e ->
                    onFailure(e)
                }
        } else {
            onFailure(Exception("User not found"))
        }
    }
}