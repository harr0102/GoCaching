package dk.itu.moapd.gocaching.controller

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Address
import android.location.Geocoder
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import com.google.android.gms.location.*
import dk.itu.moapd.gocaching.R
import dk.itu.moapd.gocaching.model.GeoCache
import dk.itu.moapd.gocaching.model.User
import dk.itu.moapd.gocaching.view.GoCachingActivity
import dk.itu.moapd.gocaching.view.MapActivity
import io.realm.Realm
import io.realm.Realm.getDefaultInstance
import kotlinx.android.synthetic.main.fragment_edit_profile.*
import kotlinx.android.synthetic.main.fragment_geo_cache.*
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class AddGeoCacheFragment : Fragment() {
    var dateFormat: SimpleDateFormat? = SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
    private lateinit var mRealm: Realm
    private var myUserId = -1
    private lateinit var myUser: User
    private val permissions: ArrayList<String> = ArrayList()
    private var lon = 0.0
    private var lat = 0.0
    private var filePath: String = ""
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var mPreferences: SharedPreferences


    companion object
    {
        private const val ALL_PERMISSIONS_RESULT = 1011
        private const val UPDATE_INTERVAL = 5000L
        private const val FASTEST_INTERVAL = 5000L


        private const val CACHE = "cache"
        private const val WHERE = "where"
        private const val USER_ID = "user_id"
        private const val FILEPATH = "filepath"
    }


    private fun updateImageView() {
        // decode the filePath into a Bitmap
        val bitmap: Bitmap = BitmapFactory.decodeFile(this.filePath)

        // Update imageView with the bitmap
        photoImage.setImageBitmap(bitmap)
    }

    fun setUserId(userId: Int) {
        this.myUserId = userId
    }

    private fun findUserWithId(userId: Int): User? {
        return mRealm.where(User::class.java)
            .equalTo("id", userId).findFirst()
    }

    @SuppressLint("UseRequireInsteadOfGet")
    private fun checkPermission() =
        ActivityCompat.checkSelfPermission(
                context!!,
                Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(
                        context!!,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED &&
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

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        if (checkPermission())
            return

        val locationRequest = LocationRequest().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = UPDATE_INTERVAL
            fastestInterval = FASTEST_INTERVAL

        }

        fusedLocationProviderClient.requestLocationUpdates(
                locationRequest, locationCallback, null
        )
    }
    private fun stopLocationUpdates() {
        fusedLocationProviderClient
            .removeLocationUpdates(locationCallback)
    }

    private fun getAddress(longitude: Double, latitude: Double): String {
        val geocoder = Geocoder(context, Locale.getDefault())
        val stringBuilder = StringBuilder()

        try {
            val addresses: List<Address> =
                geocoder.getFromLocation(latitude, longitude, 1)

            if (addresses.isNotEmpty()) {
                val address: Address = addresses[0]
                stringBuilder.apply{
                    append(address.getAddressLine(0)).append("\n")
                    append(address.locality).append("\n")
                    append(address.postalCode).append("\n")
                    append(address.countryName)
                }
            } else
                return "No address found"

        } catch (ex: IOException) {
            ex.printStackTrace()
        }

        return stringBuilder.toString()
    }

    override fun onResume()
    {
        super.onResume(
        )
        startLocationUpdates(
        )
    }

    override fun onPause()
    {
        super.onPause(
        )
        stopLocationUpdates(
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mRealm = getDefaultInstance()
        mPreferences = activity?.getPreferences(Context.MODE_PRIVATE)!!

        if (savedInstanceState != null) {
            this.myUserId = savedInstanceState.getInt(USER_ID, -1)
            this.filePath = savedInstanceState.getString(FILEPATH, "")
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {

        outState.putInt(USER_ID, this.myUserId)
        if ((activity as GoCachingActivity?)?.getFilePath().toString().isNotEmpty()) {
            outState.putString(FILEPATH, (activity as GoCachingActivity?)?.getFilePath())
        } else {
            outState.putString(FILEPATH, this.filePath)
        }

        super.onSaveInstanceState(outState)

    }



    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_geo_cache, container, false)

        // mRealm = getDefaultInstance()
        //mPreferences = activity?.getPreferences(Context.MODE_PRIVATE)!!


        permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
        permissions.add(Manifest.permission.CAMERA)
        val permissionsToRequest = permissionsToRequest(permissions)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            if (permissionsToRequest.size > 0)
                requestPermissions(
                        permissionsToRequest.toTypedArray(),
                        ALL_PERMISSIONS_RESULT
                )

        fusedLocationProviderClient = LocationServices
            .getFusedLocationProviderClient(requireContext())

        locationCallback = object : LocationCallback() {
            @SuppressLint("SetTextI18n")
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations) {
                    lon = location.longitude
                    lat = location.latitude
                    maps_button.isEnabled = true
                    lon_text.text = "Longitude: $lon"
                    lat_text.text = "Latitude: $lat"
                    where_edit.setText(getAddress(lon, lat))
                }
            }
        }





        return view
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val myUser = findUserWithId(this.myUserId)
        if (myUser != null) {
            this.myUser = myUser
        } else {
            throw Resources.NotFoundException("Couldn't catch user ID") // this should not be happening
        }

    }

    @SuppressLint("CommitPrefEdits")
    override fun onStart() {
        super.onStart()

        if ((activity as GoCachingActivity?)?.getFilePath().toString().isNotEmpty()) {
            this.filePath = (activity as GoCachingActivity?)?.getFilePath().toString()
            updateImageView()
        } else if (this.filePath.isNotEmpty()) {
            updateImageView()
        }


        maps_button.isEnabled = false

        val builder = activity?.let {
            AlertDialog.Builder(it).apply{
                setTitle("Cache name, Where, Category and Photo is required!")
                setNeutralButton(android.R.string.ok, null)
            }
        }

        val dialog = builder?.create()
        builder?.setNeutralButton("OK"){ dialog, _ ->
            dialog.dismiss()
        }


        cache_geo.setText(R.string.cache_text)
        cache_edit.setHint(R.string.cache_hint)

        where_geo.setText(R.string.where)
        where_edit.setHint(R.string.where_hint)

        category_geo.setText(R.string.category)

        commit_button.setText(R.string.add_cache_text)
        commit_button.setOnClickListener {
            if (cache_edit.text.isEmpty() || where_edit.text.isEmpty()) {
                dialog?.show()
                return@setOnClickListener

            }

            var cache = cache_edit.text.toString()
            var location = where_edit.text.toString()
            var category = category_spinner.selectedItem.toString()



            if (filePath.isEmpty()) {
                dialog?.show()
                return@setOnClickListener
            }

            mRealm.executeTransactionAsync { realm ->
                    var id = realm.where(GeoCache::class.java).max("id")
                    if (id == null) id = 0

                    val newGeoCache = GeoCache(
                            id = id.toInt() + 1,
                            cache = cache,
                            location = location,
                            category = category,
                            filePath = this.filePath,
                            user = this.myUser,
                            createdDate = dateFormat?.format(Date()).toString(),
                            lon = this.lon,
                            lat = this.lat
                    )

                        realm.insert(newGeoCache) //This is where the magic happens :))
                }

            cache_edit.text.clear()
            where_edit.text.clear()
            category_spinner.setSelection(0)
            Toast.makeText(requireActivity(), "Added cache! ", Toast.LENGTH_SHORT).show()

            //Navigate back to main page after added cache
            //TODO: Hide keyboard before we navigate back
            val manager = requireFragmentManager()
            manager.popBackStack()
        }

        // Maps Button (Opens maps!)
        maps_button.setOnClickListener {
            val intent = Intent(activity, MapActivity::class.java).apply {
                putExtra("longitude", lon)
                putExtra("latitude", lat)
            }
            startActivity(intent)
        }


    }



}