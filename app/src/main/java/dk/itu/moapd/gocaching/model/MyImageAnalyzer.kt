package dk.itu.moapd.gocaching.model

import android.annotation.SuppressLint
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.fragment.app.FragmentManager
import com.google.mlkit.vision.barcode.Barcode
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import dk.itu.moapd.gocaching.controller.ScoreFragment

class MyImageAnalyzer(
    private val fragmentManager: FragmentManager
) : ImageAnalysis.Analyzer {

    override fun analyze(imageProxy: ImageProxy) {
        scanBarcode(imageProxy)
    }

    @SuppressLint("UnsafeExperimentalUsageError", "UnsafeOptInUsageError")
    private fun scanBarcode(imageProxy: ImageProxy) {
        imageProxy.image?.let { image ->
            val inputImage = InputImage.fromMediaImage(image, imageProxy.imageInfo.rotationDegrees)
            val scanner = BarcodeScanning.getClient()
            scanner.process(inputImage)
                .addOnCompleteListener {
                    imageProxy.close()
                    if (it.isSuccessful) {
                        readBarcodeData(it.result as List<Barcode>)
                    } else {
                        it.exception?.printStackTrace()
                    }
                }
        }
    }

    private var bottomSheet = ScoreFragment()

    private fun readBarcodeData(barcodes: List<Barcode>) {
        for (barcode in barcodes) {
            when (barcode.valueType) {
                Barcode.TYPE_TEXT -> {
                    if (!bottomSheet.isAdded)
                        bottomSheet.show(fragmentManager, "")
                    bottomSheet.setUserIdFromQRcode(getGeoCacheIdFromQrCode(barcode.displayValue))
                    bottomSheet.displayGeoCache()
                    println(getGeoCacheIdFromQrCode(barcode.displayValue))
                }
            }
        }
    }

    private fun getGeoCacheIdFromQrCode(text: String?): String {
            return text!!.filter { it.isDigit() }
    }

}