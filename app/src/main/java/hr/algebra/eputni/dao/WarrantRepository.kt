package hr.algebra.eputni.dao

import hr.algebra.eputni.model.Warrant

interface WarrantRepository {
    fun startTrip(warrant: Warrant, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)
    fun endTrip(warrant: Warrant, endKilometers: Int?, description: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)
    fun getActiveWarrant(userId: String, onSuccess: (Warrant?) -> Unit, onFailure: (Exception) -> Unit)
    fun linkFilesToWarrant(warrantId: String, filesUrl: List<String>, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)
}