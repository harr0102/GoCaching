package dk.itu.moapd.gocaching

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import dk.itu.moapd.gocaching.model.GeoCache
import io.realm.Realm
import io.realm.Sort

class MapFragment : Fragment() {
    private lateinit var mRealm: Realm
    val mapFragment = SupportMapFragment.newInstance()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // List geocaches click event
        val results = mRealm.where(GeoCache::class.java)
            .sort("id", Sort.ASCENDING).findAll()

        for (geoCache in results) {
            if (geoCache.isApproved == true) {
                setMarker(geoCache.lon, geoCache.lat, geoCache.cache)
            }
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_map, container, false)
        mRealm = Realm.getDefaultInstance()

        assert(arguments != null)
        val longitude = requireArguments().getDouble("longitude")
        val latitude = requireArguments().getDouble("latitude")

        mapFragment.getMapAsync { googleMap ->
            val latLng = LatLng(latitude, longitude)
            googleMap.apply {
                addMarker(
                    MarkerOptions().position(latLng)
                        .title(resources.getString(R.string.currentLocation))
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.my_custom_marker))
                )
                mapType = GoogleMap.MAP_TYPE_NORMAL
                animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18f))
            }
        }

        childFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_map, mapFragment)
            .commit()

        return view
    }

    private fun setMarker(lat: Double, lon: Double, cache_title: String) {
        mapFragment.getMapAsync { googleMap ->
            val latLng = LatLng(lon, lat)
            googleMap.apply {
                addMarker(
                    MarkerOptions().position(latLng)
                        .title(cache_title)
                )
            }
        }
        Toast.makeText(requireActivity(), "Marker placed: $cache_title", Toast.LENGTH_SHORT).show()
    }
}