package hr.algebra.eputni.dao

import hr.algebra.eputni.model.Vehicle

interface VehicleRepository {
    suspend fun fetchVehicles(userId: String, onSuccess: (List<Vehicle>) -> Unit, onFailure: (Exception) -> Unit)
    suspend fun saveVehicle(vehicle: Vehicle, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)
    suspend fun updateVehicle(vehicle: Vehicle, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)
    suspend fun deleteVehicle(vehicle: Vehicle, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)
}