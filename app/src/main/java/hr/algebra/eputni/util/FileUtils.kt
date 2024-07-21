package hr.algebra.eputni.util

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import hr.algebra.eputni.dao.WarrantRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Suppress("DEPRECATION")
class FileUtils(
    private val activity: Activity?,
    private val fragment: Fragment?,
    private val warrantRepository: WarrantRepository,
    private val message: String
) {
    private val PICK_PDF_REQUEST = 1
    private val userId = FirebaseAuth.getInstance().currentUser?.uid
    private val scope = CoroutineScope(Dispatchers.IO)

    fun selectPdfFile() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        fragment?.startActivityForResult(
            Intent.createChooser(intent, message),
            PICK_PDF_REQUEST
        )
    }

    fun handleActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == PICK_PDF_REQUEST && resultCode == Activity.RESULT_OK) {
            val selectedUris = mutableListOf<Uri>()
            data?.clipData?.let { clipData ->
                for (i in 0 until clipData.itemCount) {
                    selectedUris.add(clipData.getItemAt(i).uri)
                }
            } ?: data?.data?.let { uri ->
                selectedUris.add(uri)
            }
            if (selectedUris.isNotEmpty()) {
                scope.launch {
                    uploadFileToFirebase(selectedUris)
                }
            }
        }
    }

    fun uploadFileToFirebase(uris: List<Uri>) {
        scope.launch {
            val storageRef = FirebaseStorage.getInstance().reference
            val fileUrls = mutableListOf<String>()

            try {
                uris.forEach { uri ->
                    val fileRef =
                        storageRef.child("warrants/${userId}/${System.currentTimeMillis()}.pdf")

                    fileRef.putFile(uri).await()
                    fileUrls.add(fileRef.downloadUrl.await().toString())
                }
                linkFilesToWarrant(fileUrls)
            } catch (e: Exception) {
                CoroutineScope(Dispatchers.Main).launch {
                    Toast.makeText(fragment?.requireContext() ?: activity, e.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private suspend fun linkFilesToWarrant(fileUrls: List<String>) {
        try {
            warrantRepository.getActiveWarrant(userId!!,
                onSuccess = { warrant ->
                    scope.launch {
                        warrantRepository.linkFilesToWarrant(warrant!!.id!!, fileUrls,
                            onSuccess = {
                                CoroutineScope(Dispatchers.Main).launch {
                                    if (fileUrls.size == 1)
                                        Toast.makeText(
                                            fragment?.requireContext() ?: activity,
                                            "Datoteka spremljena",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    else
                                        Toast.makeText(
                                            fragment?.requireContext() ?: activity,
                                            "Datoteke spremljene",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                }
                            }, {
                                Toast.makeText(
                                    fragment?.requireContext() ?: activity,
                                    it.message,
                                    Toast.LENGTH_SHORT
                                ).show()
                            })
                    }
                },
                onFailure = {
                    Toast.makeText(fragment?.requireContext() ?: activity, it.message, Toast.LENGTH_SHORT).show()
                })
        } catch (e: Exception) {
            CoroutineScope(Dispatchers.Main).launch {
                Toast.makeText(fragment?.requireContext() ?: activity, e.message, Toast.LENGTH_SHORT).show()
            }
        }
    }
}