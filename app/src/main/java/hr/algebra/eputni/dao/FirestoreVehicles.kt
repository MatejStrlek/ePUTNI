package hr.algebra.eputni.dao

import com.google.firebase.firestore.FirebaseFirestore
import hr.algebra.eputni.model.Vehicle

class FirestoreVehicles : VehicleRepository {
    private val db = FirebaseFirestore.getInstance()

    override fun fetchVehicles(
        userId: String,
        onSuccess: (List<Vehicle>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        db.collection("vehicles")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener {
                val vehicles = it.toObjects(Vehicle::class.java)
                onSuccess(vehicles)
            }
            .addOnFailureListener {
                onFailure(it)
            }
    }

    override fun saveVehicle(vehicle: Vehicle, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        db.collection("vehicles")
            .add(vehicle)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener {
                onFailure(it)
            }
    }

}