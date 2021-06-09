package dk.itu.moapd.gocaching.controller

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import dk.itu.moapd.gocaching.R
import dk.itu.moapd.gocaching.model.GeoCache
import dk.itu.moapd.gocaching.model.User
import dk.itu.moapd.gocaching.view.GoCachingActivity
import io.realm.Realm
import kotlinx.android.synthetic.main.fragment_geo_cache.*
import kotlinx.android.synthetic.main.list_geo_cache.*
import java.text.SimpleDateFormat
import java.util.*


class EditGeoCacheFragment : Fragment() {
    lateinit var myGeoCache: GeoCache
    private var myGeoCacheId = -1
    private var myUserId = -1
    private lateinit var myUser: User
    private lateinit var mRealm: Realm
    var hasPhotoChanged: Boolean = true
    private var filePath: String = ""
    private lateinit var mPreferences: SharedPreferences

    var dateFormat: SimpleDateFormat? = SimpleDateFormat("yyyy/MM/dd HH:mm:ss")

    companion object
    {
        private const val GEOCACHE_ID = "geocache_id"
        private const val USER_ID = "user_id"
        private const val FILEPATH = "filepath"
    }

    fun setGeoCacheId(GeoCacheId: Int) {
        this.myGeoCacheId = GeoCacheId
    }


    fun setUserId(userId: Int) {
        this.myUserId = userId
    }

    private fun setRealmObjects() {
        this.myGeoCache = findGeoCacheWithId(this.myGeoCacheId)!!
        this.myUser = findUserWithId(this.myUserId)!!
    }

    private fun findGeoCacheWithId(geoCacheId: Int): GeoCache? {
        return mRealm.where(GeoCache::class.java)
                .equalTo("id", geoCacheId).findFirst()
    }

    private fun findUserWithId(userId: Int): User? {
        return mRealm.where(User::class.java)
                .equalTo("id", userId).findFirst()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mRealm = Realm.getDefaultInstance()
        mPreferences = activity?.getPreferences(Context.MODE_PRIVATE)!!

        if (savedInstanceState != null) {
            this.myGeoCacheId = savedInstanceState.getInt(GEOCACHE_ID, -1)
            this.myUserId = savedInstanceState.getInt(USER_ID, -1)
            this.filePath = savedInstanceState.getString(FILEPATH, "")

        }
    }

    override fun onSaveInstanceState(outState: Bundle) {

        outState.putInt(GEOCACHE_ID, this.myGeoCacheId)
        outState.putInt(USER_ID, this.myUserId)
        if ((activity as GoCachingActivity?)?.getFilePath().toString().isNotEmpty()) {
            outState.putString(FILEPATH, (activity as GoCachingActivity?)?.getFilePath())
        } else {
            outState.putString(FILEPATH, this.filePath)
        }
        super.onSaveInstanceState(outState)

    }


    private fun isAdminAndIsNotApproved(): Boolean {
        // return true if isAdmin = true and isApproved = false
        if (myUser.isAdmin) {
            return myGeoCache.isApproved != true
        }
        return false
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.fragment_geo_cache, menu)

        val approve = menu.findItem(R.id.approve_cache)

        approve.isVisible = isAdminAndIsNotApproved()


    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.delete_cache -> {
                deleteGeoCache(myGeoCache)
                Toast.makeText(context, "${myGeoCache.cache} deleted!", Toast.LENGTH_SHORT).show()
                val manager = requireFragmentManager()
                manager.popBackStack()
                return true
            }
            R.id.approve_cache -> {
                updateGeoCache(true, this.hasPhotoChanged)
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun deleteGeoCache(gc: GeoCache) {
        val id = gc.id

        mRealm.executeTransactionAsync { realm ->
            // Find the GeoCache in the database by Id:
            val geoCacheFromDB = realm.where(GeoCache::class.java)
                .equalTo("id", id).findFirst()
            geoCacheFromDB?.deleteFromRealm()
        }
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_geo_cache, container, false)
        setHasOptionsMenu(true)
        mRealm = Realm.getDefaultInstance()

        return view
    }

    private fun updateImageView() {
        // decode the filePath into a Bitmap
        val bitmap: Bitmap = BitmapFactory.decodeFile(filePath)

        // Update imageView with the bitmap
        photoImage.setImageBitmap(bitmap)
    }

    override fun onStart() {
        super.onStart()


        setRealmObjects()
        this.hasPhotoChanged = true


        if (this.filePath.isEmpty()) {
            this.filePath = myGeoCache.filePath
            this.hasPhotoChanged = false
        } else if (this.filePath == myGeoCache.filePath) {
            this.hasPhotoChanged = false
        }

        if ((activity as GoCachingActivity?)?.getFilePath().toString().isNotEmpty()) {
            this.filePath = (activity as GoCachingActivity?)?.getFilePath().toString()
            updateImageView()
        } else {
            updateImageView()
        }


        cache_geo.setText(R.string.cache_text)
        cache_edit.setHint(myGeoCache.cache)

        where_geo.setText(R.string.where)
        where_edit.setHint(myGeoCache.location)

        category_geo.setText(R.string.category)
        if (myGeoCache.category == "Easy") category_spinner.setSelection(0)
        if (myGeoCache.category == "Medium") category_spinner.setSelection(1)
        if (myGeoCache.category == "Hard") category_spinner.setSelection(2)

        commit_button.setText(R.string.update)
        commit_button.setOnClickListener {
             // If user all fields are empty // do nothing
            if (cache_edit.text.toString().isEmpty() && where_edit.text.toString().isEmpty() && category_spinner.selectedItem == myGeoCache.category && !this.hasPhotoChanged) {
                Toast.makeText(requireActivity(), "Nothing to update", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }



            updateGeoCache(false, this.hasPhotoChanged)


         }
    }

    fun updateGeoCache(isApproved: Boolean, hasPhotoChanged: Boolean) {

        val id = if (myGeoCache != null) myGeoCache?.id else -1
        var updatedCacheText = ""
        var updatedLocationText = ""
        var updatedCategoryText = ""

        // If admin approves but doesn't change picture : DON'T CHANGE AT ALL
        if (isApproved && !hasPhotoChanged) {
            updatedCacheText = myGeoCache.cache
            updatedLocationText = myGeoCache.location
            updatedCategoryText = myGeoCache.category
        }

        // If user has only changed cache: CHANGE CACHE, ONLY
        if (cache_edit.text.toString().isNotEmpty()) {
            updatedCacheText = cache_edit.text.toString()
            updatedLocationText = myGeoCache.location
            updatedCategoryText = myGeoCache.category
        }

        // If user has only changed location: CHANGE LOCATION, ONLY
        if (where_edit.text.toString().isNotEmpty()) {
            updatedCacheText = myGeoCache.cache
            updatedLocationText = where_edit.text.toString()
            updatedCategoryText = myGeoCache.category
        }
        // If user has only changed category: CHANGE CATEGORY, ONLY
        if (category_spinner.selectedItem != myGeoCache.category){
            updatedCacheText = myGeoCache.cache
            updatedLocationText = myGeoCache.location
            updatedCategoryText = category_spinner.selectedItem.toString()

        }

        // If user has changed all fields: UPDATE all
        if (cache_edit.text.toString().isNotEmpty() && where_edit.text.toString().isNotEmpty() && category_spinner.selectedItem != myGeoCache.category)
        {
            updatedCacheText = cache_edit.text.toString()
            updatedLocationText = where_edit.text.toString()
            updatedCategoryText = category_spinner.selectedItem.toString()
        }

        // If user has changed none fields BUT has taken a new photo: CHANGE PHOTO ONLY
        if (hasPhotoChanged && cache_edit.text.toString().isEmpty() && where_edit.text.toString().isEmpty() && category_spinner.selectedItem == myGeoCache.category) {
            updatedCacheText = myGeoCache.cache
            updatedLocationText = myGeoCache.location
            updatedCategoryText = myGeoCache.category
        }



        mRealm.executeTransactionAsync { realm ->
            val geoCacheFromDB = realm.where(GeoCache::class.java)
                    .equalTo("id", id).findFirst()
            geoCacheFromDB?.cache = updatedCacheText
            geoCacheFromDB?.location = updatedLocationText
            geoCacheFromDB?.category = updatedCategoryText
            geoCacheFromDB?.updatedDate = dateFormat?.format(Date()).toString()
            geoCacheFromDB?.isApproved = isApproved
            geoCacheFromDB?.filePath = this.filePath
        }
        Toast.makeText(requireActivity(), "Successfully updated", Toast.LENGTH_SHORT).show()
        //TODO: Hide keyboard before we navigate back
        val manager = requireFragmentManager()
        manager.popBackStack()
    }

}