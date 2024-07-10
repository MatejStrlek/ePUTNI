package hr.algebra.eputni.dao

import com.google.firebase.firestore.FirebaseFirestore
import hr.algebra.eputni.model.Vehicle

class FirestoreVehicles : VehicleRepository {
    private val db = FirebaseFirestore.getInstance()
    private val VEHICLES: String = "vehicles"

    override fun fetchVehicles(
        userId: String,
        onSuccess: (List<Vehicle>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        db.collection(VEHICLES)
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { result ->
                val vehicles = result.map { document ->
                    val vehicle = document.toObject(Vehicle::class.java)
                    vehicle.id = document.id
                    vehicle
                }
                onSuccess(vehicles)
            }
            .addOnFailureListener {
                onFailure(it)
            }
    }

    override fun saveVehicle(
        vehicle: Vehicle,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit) {
        val documentRef = db.collection(VEHICLES).document()
        vehicle.id = documentRef.id

        documentRef.set(vehicle)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener {
                onFailure(it)
            }
    }

    override fun updateVehicle(
        vehicle: Vehicle,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        db.collection(VEHICLES).document(vehicle.id)
            .set(vehicle)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener {
                onFailure(it)
            }
    }

    override fun deleteVehicle(
        vehicle: Vehicle,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        db.collection(VEHICLES).document(vehicle.id)
            .delete()
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener {
                onFailure(it)
            }
    }
}