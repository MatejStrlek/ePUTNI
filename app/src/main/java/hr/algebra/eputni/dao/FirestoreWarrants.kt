package hr.algebra.eputni.dao

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import hr.algebra.eputni.model.Warrant
import kotlinx.coroutines.tasks.await

class FirestoreWarrants: WarrantRepository {
    private val db = FirebaseFirestore.getInstance()
    private val WARRANTS: String = "warrants"

    override suspend fun startTrip(warrant: Warrant, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val documentRef = db.collection(WARRANTS).document()
        warrant.id = documentRef.id

        documentRef.set(warrant)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener {
                onFailure(it)
            }
            .await()
    }

    override suspend fun endTrip(
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
            .await()
    }

    override suspend fun getActiveWarrant(
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
            .await()
    }

    override suspend fun linkFilesToWarrant(
        warrantId: String,
        filesUrl: List<String>,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        db.collection(WARRANTS).document(warrantId)
            .update("files", FieldValue.arrayUnion(*filesUrl.toTypedArray()))
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener {
                onFailure(it)
            }
            .await()
    }
}