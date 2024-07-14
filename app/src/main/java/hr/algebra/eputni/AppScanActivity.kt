package hr.algebra.eputni

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.zynksoftware.documentscanner.ScanActivity
import com.zynksoftware.documentscanner.model.DocumentScannerErrorModel
import com.zynksoftware.documentscanner.model.ScannerResults

class AppScanActivity : ScanActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app_scan)
        addFragmentContentLayout()
    }

    override fun onError(error: DocumentScannerErrorModel) {
        Toast.makeText(this, error.errorMessage?.name , Toast.LENGTH_SHORT).show()
    }

    override fun onSuccess(scannerResults: ScannerResults) {
        val croppedImageFile = scannerResults.croppedImageFile
        val storageRef = FirebaseStorage.getInstance().reference
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (croppedImageFile != null) {
            val fileRef = storageRef.child("warrants/${userId}/${System.currentTimeMillis()}.jpg")
            fileRef.putFile(Uri.fromFile(croppedImageFile))
                .addOnSuccessListener {
                    fileRef.downloadUrl.addOnSuccessListener { url ->
                        val resultIntent = Intent().apply {
                            putExtra("scannedFileUrl", url.toString())
                        }
                        setResult(Activity.RESULT_OK, resultIntent)
                        finish()
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
                    setResult(Activity.RESULT_CANCELED)
                    finish()
                }
        }
        else {
            Toast.makeText(this, "No image was captured", Toast.LENGTH_SHORT).show()
            setResult(Activity.RESULT_CANCELED)
            finish()
        }
    }

    override fun onClose() {
        finish()
    }
}