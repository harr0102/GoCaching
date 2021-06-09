package dk.itu.moapd.gocaching.view

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Size
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.common.util.concurrent.ListenableFuture
import dk.itu.moapd.gocaching.R
import dk.itu.moapd.gocaching.model.GeoCache
import dk.itu.moapd.gocaching.model.MyImageAnalyzer
import dk.itu.moapd.gocaching.model.User
import io.realm.Realm

import kotlinx.android.synthetic.main.activity_qr.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class ScanQrActivity : AppCompatActivity() {

    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var analyzer: MyImageAnalyzer
    private lateinit var mRealm: Realm
    private lateinit var myUser: User
    private var myUserId = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qr)
        checkCameraPermission()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            @Suppress("DEPRECATION")
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
        mRealm = Realm.getDefaultInstance()
        this.myUserId = intent.getIntExtra("userId", -1)
        val myUser = findUserWithId(myUserId)


        if (myUser != null) {
            this.myUser = myUser

        } else {
            throw Resources.NotFoundException("Couldn't catch user ID") // this should not be happening
        }

        analyzer = MyImageAnalyzer(supportFragmentManager)
        cameraExecutor = Executors.newSingleThreadExecutor()
        cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener(Runnable {
            val cameraProvider = cameraProviderFuture.get()
            bindPreview(cameraProvider)
        }, ContextCompat.getMainExecutor(this))
    }

    @SuppressLint("UnsafeExperimentalUsageError")
    private fun bindPreview(cameraProvider: ProcessCameraProvider) {
        val preview: Preview = Preview.Builder()
            .build()
        val cameraSelector: CameraSelector = CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
            .build()
        preview.setSurfaceProvider(previewView.surfaceProvider)

        val imageAnalysis = ImageAnalysis.Builder()
            .setTargetResolution(Size(1280, 720))
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
        imageAnalysis.setAnalyzer(cameraExecutor, analyzer)

        cameraProvider.bindToLifecycle(
            this as LifecycleOwner,
            cameraSelector,
            imageAnalysis,
            preview
        )
    }



    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Intent().also {
                it.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                it.data = Uri.fromParts("package", packageName, null)
                startActivity(it)
                finish()
            }
        }
    }

    private fun findUserWithId(userId: Int): User? {
        return mRealm.where(User::class.java)
            .equalTo("id", userId).findFirst()
    }

    fun findGeoCacheWithId(geoCacheId: Int?): GeoCache? {
        return mRealm.where(GeoCache::class.java)
            .equalTo("id", geoCacheId).findFirst()
    }

    private fun userHasGeoCache(geoCache: GeoCache?): Boolean {
        myUser.geoCaches?.forEach { gc ->
            if (gc.id == geoCache?.id) {
                return true
            }
        }
        return false
    }

    fun addGeoCacheToUser(geoCacheId: Int?) {
        if (userHasGeoCache(findGeoCacheWithId(geoCacheId))) { // stop here if user already has GeoCache
            toast("You already found this cache")
        } else {
            // add GeoCache to Users' list of GeoCaches
            mRealm.executeTransactionAsync { realm ->
                val userFromDB = realm.where(User::class.java)
                    .equalTo("id", this.myUserId).findFirst()
                    userFromDB?.geoCaches?.add(
                        realm.where(GeoCache::class.java)
                            .equalTo("id", geoCacheId).findFirst()
                    )
            }

            toast("Your points have been added!")
        }
    }





    fun Context.toast(message: CharSequence) =
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

}