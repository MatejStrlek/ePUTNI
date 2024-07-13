package hr.algebra.eputni.dao

import com.google.firebase.firestore.FirebaseFirestore
import hr.algebra.eputni.model.Warrant

class FirestoreWarrants: WarrantRepository {
    private val db = FirebaseFirestore.getInstance()
    private val WARRANTS: String = "warrants"

    override fun startTrip(warrant: Warrant, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val documentRef = db.collection(WARRANTS).document()
        warrant.id = documentRef.id

        documentRef.set(warrant)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener {
                onFailure(it)
            }
    }

    override fun endTrip(
        warrant: Warrant,
        endKilometers: Int?,
        description: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val updateData = mutableMapOf<String, Any>(
            "finished" to true,
            "endTime" to System.currentTimeMillis(),
            "description" to description
        )

        if (endKilometers != null) {
            updateData["endKilometers"] = endKilometers
        }

        db.collection(WARRANTS).document(warrant.id!!)
            .update(updateData)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener {
                onFailure(it)
            }
    }

    override fun getActiveWarrant(
        userId: String,
        onSuccess: (Warrant?) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        db.collection(WARRANTS)
            .whereEqualTo("userId", userId)
            .whereEqualTo("finished", false)
            .get()
            .addOnSuccessListener { documents ->
                if (documents != null && !documents.isEmpty) {
                    val warrant = documents.documents[0].toObject(Warrant::class.java)
                    onSuccess(warrant)
                } else {
                    onSuccess(null)
                }
            }
            .addOnFailureListener {
                onFailure(it)
            }
    }
}