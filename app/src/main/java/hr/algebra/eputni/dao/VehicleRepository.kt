package hr.algebra.eputni.dao

import hr.algebra.eputni.model.Vehicle

interface VehicleRepository {
    fun fetchVehicles(userId: String, onSuccess: (List<Vehicle>) -> Unit, onFailure: (Exception) -> Unit)
    fun saveVehicle(vehicle: Vehicle, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)
    fun updateVehicle(vehicle: Vehicle, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)
    fun deleteVehicle(vehicle: Vehicle, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)
}