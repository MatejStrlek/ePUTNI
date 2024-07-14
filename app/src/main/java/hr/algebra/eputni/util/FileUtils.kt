package hr.algebra.eputni.util

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import hr.algebra.eputni.dao.WarrantRepository

@Suppress("DEPRECATION")
class FileUtils(
    private val fragment: Fragment,
    private val warrantRepository: WarrantRepository,
    private val message: String
) {
    private val PICK_PDF_REQUEST = 1
    private val userId = FirebaseAuth.getInstance().currentUser?.uid

    fun selectPdfFile() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        fragment.startActivityForResult(
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
                uploadFileToFirebase(selectedUris)
            }
        }
    }

    private fun uploadFileToFirebase(uris: List<Uri>) {
        val storageRef = FirebaseStorage.getInstance().reference
        val fileUrls = mutableListOf<String>()

        uris.forEach { uri ->
            val fileRef = storageRef.child("warrants/${userId}/${System.currentTimeMillis()}.pdf")
            fileRef.putFile(uri)
                .addOnSuccessListener {
                    fileRef.downloadUrl.addOnSuccessListener { uri ->
                        fileUrls.add(uri.toString())
                        if (fileUrls.size == uris.size) {
                            linkFileToWarrant(fileUrls)
                        }
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(fragment.requireContext(), it.message, Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun linkFileToWarrant(fileUrls: List<String>) {
        warrantRepository.getActiveWarrant(userId!!,
            onSuccess = { warrant ->
                warrantRepository.linkFilesToWarrant(warrant!!.id!!, fileUrls,
                    onSuccess = {
                        if (fileUrls.size == 1)
                            Toast.makeText(fragment.requireContext(), "Datoteka spremljena", Toast.LENGTH_SHORT).show()
                        else
                            Toast.makeText(fragment.requireContext(), "Datoteke spremljene", Toast.LENGTH_SHORT).show()
                    }, {
                        Toast.makeText(fragment.requireContext(), it.message, Toast.LENGTH_SHORT).show()
                    })
        }, {
            Toast.makeText(fragment.requireContext(), it.message, Toast.LENGTH_SHORT).show()
        })
    }
}