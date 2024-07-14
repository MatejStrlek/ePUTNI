package hr.algebra.eputni.scanner

import android.app.Application
import android.graphics.Bitmap
import com.zynksoftware.documentscanner.ui.DocumentScanner

class ScannerApplication: Application() {
    override fun onCreate() {
        super.onCreate()

        val configuration = DocumentScanner.Configuration()
        configuration.imageQuality = 100
        configuration.imageSize = 2000000
        configuration.imageType = Bitmap.CompressFormat.JPEG
        DocumentScanner.init(this, configuration)
    }
}