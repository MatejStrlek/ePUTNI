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
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        fragment.startActivityForResult(
            Intent.createChooser(intent, message),
            PICK_PDF_REQUEST
        )
    }

    fun handleActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == PICK_PDF_REQUEST && resultCode == Activity.RESULT_OK) {
            data?.data?.let {uri ->
                uploadFileToFirebase(uri)
            }
        }
    }

    private fun uploadFileToFirebase(uri: Uri) {
        val storageRef = FirebaseStorage.getInstance().reference
        val fileRef = storageRef.child("warrants/${userId}/${System.currentTimeMillis()}.pdf")

        fileRef.putFile(uri)
            .addOnSuccessListener {
                fileRef.downloadUrl.addOnSuccessListener {url ->
                    linkFileToWarrant(url.toString())
                }
            }
            .addOnFailureListener {
                Toast.makeText(fragment.context, "Failed: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun linkFileToWarrant(filesUrl: String) {
        warrantRepository.getActiveWarrant(userId!!,
            onSuccess = { warrant ->
                warrantRepository.linkFilesToWarrant(warrant!!.id!!, listOf(filesUrl),
                    onSuccess = {
                        Toast.makeText(fragment.requireContext(), "Datoteka spremljena", Toast.LENGTH_SHORT).show()
                    }, {
                        Toast.makeText(fragment.requireContext(), it.message, Toast.LENGTH_SHORT).show()
                    })
        }, {
            Toast.makeText(fragment.requireContext(), it.message, Toast.LENGTH_SHORT).show()
        })
    }
}