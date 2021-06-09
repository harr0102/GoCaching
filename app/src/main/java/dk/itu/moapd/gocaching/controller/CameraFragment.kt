package dk.itu.moapd.gocaching.controller

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.*
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import dk.itu.moapd.gocaching.viewmodel.CameraVM
import dk.itu.moapd.gocaching.R
import io.realm.Realm
import kotlinx.android.synthetic.main.fragment_camera.*
import org.opencv.android.*
import org.opencv.core.Core
import org.opencv.core.CvType.*
import org.opencv.core.Mat
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgproc.Imgproc
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


class CameraFragment : Fragment(), View.OnTouchListener, CameraBridgeViewBase.CvCameraViewListener2 {
    private val model: CameraVM by activityViewModels()
    private lateinit var mRealm: Realm

    private var cont = 0
    private var index = 0

    private var rgbaImage: Mat? = null

    private lateinit var loaderCallback: BaseLoaderCallback
    private val permissions: ArrayList<String> = ArrayList()

    companion object
    {
        private const val ALL_PERMISSIONS_RESULT = 1011
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        mRealm = Realm.getDefaultInstance()


        permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        val permissionsToRequest = permissionsToRequest(permissions)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            if (permissionsToRequest.size > 0)
                requestPermissions(
                        permissionsToRequest.toTypedArray(),
                        ALL_PERMISSIONS_RESULT
                )

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_camera, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        camera_view.visibility = SurfaceView.VISIBLE
        camera_view.setCameraIndex(index)
        camera_view.setCvCameraViewListener(this)

        camera_button.setOnClickListener {
            index = index xor 1
            camera_view.disableView()
            camera_view.setCameraIndex(index)
            camera_view.enableView()
        }

        //TODO: kald på dennem metode fra cameraactivity
        /*capture_button.setOnClickListener {
            val bgrImage = rgbaImage!!.clone()
            Imgproc.cvtColor(bgrImage, bgrImage, Imgproc.COLOR_RGBA2BGR, 3)

            val path = getPhotoFileUri()
            val file = File(path.path.toString())
            model.setData("hello matha")


        }*/

        loaderCallback = object : BaseLoaderCallback(activity) {
            override fun onManagerConnected(status: Int) {
                if (status == LoaderCallbackInterface.SUCCESS) {
                    camera_view.enableView()
                    camera_view.setOnTouchListener(this@CameraFragment)
                } else
                    super.onManagerConnected(status)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (!OpenCVLoader.initDebug())
            OpenCVLoader.initAsync(
                    OpenCVLoader.OPENCV_VERSION,
                    activity, loaderCallback
            )
        else
            loaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS)
    }

    override fun onPause() {
        super.onPause()
        if (camera_view != null)
            camera_view.disableView()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (camera_view != null)
            camera_view.disableView()
    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        cont++
        cont %= 4
        return v!!.performClick()
    }

    override fun onCameraViewStarted(width: Int, height: Int) {
        rgbaImage = Mat(height, width, CV_8UC4)
    }

    override fun onCameraViewStopped() {
        rgbaImage?.release()
    }

    override fun onCameraFrame(inputFrame: CameraBridgeViewBase.CvCameraViewFrame?): Mat {

        val image = inputFrame?.rgba()
        rgbaImage = image!!.clone()

        if (index == 1)
            Core.flip(image, image, 1)

        when (cont) {
            1 -> return convertToGrayscale(image)
            2 -> return convertToBGRA(image)
            3 -> return convertToCanny(image)
        }

        return image
    }

    private fun convertToGrayscale(image: Mat?): Mat {
        val grayscale = Mat()
        Imgproc.cvtColor(image, grayscale, Imgproc.COLOR_RGBA2GRAY)
        return grayscale
    }

    private fun convertToBGRA(image: Mat?): Mat {
        val bgra = Mat()
        Imgproc.cvtColor(image, bgra, Imgproc.COLOR_RGBA2BGRA)
        return bgra
    }

    private fun convertToCanny(image: Mat?): Mat {
        val grayscale = convertToGrayscale(image)

        val thresh = Mat()
        val otsuThresh = Imgproc.threshold(
                grayscale, thresh,
                0.0, 255.0, Imgproc.THRESH_BINARY or Imgproc.THRESH_OTSU
        )

        val canny = Mat()
        Imgproc.Canny(grayscale, canny, otsuThresh * 0.5, otsuThresh)

        grayscale.release()
        thresh.release()

        return canny
    }

    fun myMethod() {
        if (checkPermission())
            return

        val bgrImage = rgbaImage!!.clone()
        Imgproc.cvtColor(bgrImage, bgrImage, Imgproc.COLOR_RGBA2BGR, 3)

        val path = getPhotoFileUri()
        val file = File(path.path.toString())


        model.setData(file.toString())

        if (Imgcodecs.imwrite(file.toString(), bgrImage))
            Toast.makeText(
                    requireContext(),
                    "The image has been successfully saved",
                    Toast.LENGTH_LONG
            ).show()

    }

    private fun getPhotoFileUri(): Uri {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "IMG_${timeStamp}.jpg"

        var uri: Uri? = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val resolver = requireContext().contentResolver
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                put(MediaStore.MediaColumns.RELATIVE_PATH, "DCIM/app_name/")
            }

            uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        }

        return getUriForPreQ(fileName)
    }

    private fun getUriForPreQ(fileName: String): Uri {
        val dir = requireContext().getExternalFilesDir(Environment.DIRECTORY_DCIM)
        val photoFile = File(dir, "/GoCaching/$fileName")
        if (photoFile.parentFile?.exists() == false) photoFile.parentFile?.mkdir()
        return FileProvider.getUriForFile(
                requireContext(),
                "dk.itu.moapd.gocaching.fileprovider",
                photoFile
        )
    }



    private fun checkPermission() =
        ActivityCompat.checkSelfPermission(
                context!!,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) != PackageManager.PERMISSION_GRANTED  &&
                ActivityCompat.checkSelfPermission(context!!, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED

    private fun permissionsToRequest(
            permissions: ArrayList<String>
    ): ArrayList<String> {
        val result: ArrayList<String> = ArrayList()
        for (permission in permissions)
            if (!hasPermission(permission))
                result.add(permission)
        return result
    }

    private fun hasPermission(permission: String) =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            activity?.checkSelfPermission(permission) ==
                    PackageManager.PERMISSION_GRANTED
        else
            true

}
