package hr.algebra.eputni.dao

import hr.algebra.eputni.model.Warrant

interface WarrantRepository {
    suspend fun startTrip(warrant: Warrant, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)
    suspend fun endTrip(warrant: Warrant, description: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)
    suspend fun getActiveWarrant(userId: String, onSuccess: (Warrant?) -> Unit, onFailure: (Exception) -> Unit)
    suspend fun linkFilesToWarrant(warrantId: String, filesUrl: List<String>, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)
    suspend fun fetchWarrants(userId: String, onSuccess: (List<Warrant>) -> Unit, onFailure: (Exception) -> Unit)
}