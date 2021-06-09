package dk.itu.moapd.gocaching.controller

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dk.itu.moapd.gocaching.R
import dk.itu.moapd.gocaching.view.ScanQrActivity
import kotlinx.android.synthetic.main.fragment_bottom.*


class ScoreFragment : BottomSheetDialogFragment() {
    private lateinit var geoCacheIdFromQRcode: String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_bottom, container, false)


    fun setUserIdFromQRcode(userIdFromQRcode: String) {
        this.geoCacheIdFromQRcode = userIdFromQRcode
    }

    @SuppressLint("SetTextI18n")
    fun displayGeoCache() {
        val foundGeoCache = (activity as ScanQrActivity?)?.findGeoCacheWithId(geoCacheIdFromQRcode.toInt())
        var title: String
        var descr: String
        var points = ""
        if (foundGeoCache != null && foundGeoCache.isApproved == true) {
            title = "${foundGeoCache.cache} found!"
            descr = "You have found ${foundGeoCache.user?.firstName}s' GeoCache!"
            if (foundGeoCache.category == "Easy") points = "COLLECT 200 POINTS"
            if (foundGeoCache.category == "Medium") points = "COLLECT 500 POINTS"
            if (foundGeoCache.category == "Hard") points = "COLLECT 1000 POINTS"
        } else {
            title = "GeoCache does not exist!"
            descr = "Invalid GeoCache"
            points = "Go back"
        }
        view?.apply {
            text_view_title?.text = title
            text_view_desc?.text = descr
            text_view_collect_points?.text = points
            text_view_collect_points.setOnClickListener { _ ->
                 if (points.isEmpty() || points == "Go back") {
                     fragmentManager?.popBackStack()
                } else {
                     (activity as ScanQrActivity?)?.addGeoCacheToUser(geoCacheIdFromQRcode.toInt())
                     fragmentManager?.popBackStack()
                 }
            }
        }
    }


}