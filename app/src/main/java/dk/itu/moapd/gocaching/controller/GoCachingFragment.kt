package dk.itu.moapd.gocaching.controller


import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.view.*
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import dk.itu.moapd.gocaching.R
import dk.itu.moapd.gocaching.model.GeoCache
import dk.itu.moapd.gocaching.model.User
import dk.itu.moapd.gocaching.view.ScanQrActivity
import io.realm.OrderedRealmCollection
import io.realm.Realm
import io.realm.RealmRecyclerViewAdapter
import io.realm.Sort
import kotlinx.android.synthetic.main.activity_go_caching.*
import kotlinx.android.synthetic.main.fragment_go_caching.*
import kotlinx.android.synthetic.main.list_geo_cache.*
import java.util.*


class GoCachingFragment : Fragment() {
    private lateinit var mRealm: Realm
    private lateinit var myUser: User
    private var mCurrentTemperature = Float.NaN
    private lateinit var mSensorManager: SensorManager
    private lateinit var mTimer: Timer
    private var userId: Int = -1


    private val mTemperatureListener: SensorEventListener =
            object : SensorEventListener {

                override fun onSensorChanged(sensorEvent: SensorEvent) {
                    mCurrentTemperature = sensorEvent.values[0]
                }

                override fun onAccuracyChanged(sensor: Sensor, i: Int) {}
            }



    private fun findUserWithId(userId: Int): User? {
        return mRealm.where(User::class.java)
                .equalTo("id", userId).findFirst()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mRealm = Realm.getDefaultInstance()

        // menu
        setHasOptionsMenu(true)

    }


    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_go_caching, container, false)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


            assert(arguments != null)
            this.userId = requireArguments().getInt("id")


        val myUser = findUserWithId(this.userId)

        if (myUser != null) {
            this.myUser = myUser
        } else {
            throw Resources.NotFoundException("Couldn't catch user ID") // this should not be happening
        }

        val allUsersFromDB = mRealm.where(User::class.java)
                .sort("id", Sort.ASCENDING).findAll()
        val spinner = spinner
        val spinnerAdapter: ArrayAdapter<String>? = context?.let { ArrayAdapter(it, android.R.layout.simple_spinner_item, android.R.id.text1) }
        spinnerAdapter?.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = spinnerAdapter

        spinnerAdapter?.add(resources.getString(R.string.myCaches))
        for (user in allUsersFromDB) {
                spinnerAdapter?.add("${user.firstName} ${user.lastName}'s caches")
        }

        spinnerAdapter?.notifyDataSetChanged()

        temperature.keyListener = null
        mSensorManager = requireActivity()
                .getSystemService(Context.SENSOR_SERVICE) as SensorManager

        mTimer = Timer()
        mTimer.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                updateUI()
            }
        }, 0, 100)


        // as default listAssociatedCachesToUser to myUser
        listAssociatedCachesToUser(myUser)

        // calculate a users total points and display
        edit_text_total_points.setText(calcTotalPoints(myUser).toString())


    }


    override fun onResume() {
        super.onResume()
        if (myUser != null) edit_text_total_points.setText(calcTotalPoints(myUser).toString())
        val valTemperature = mSensorManager
                .getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE)
        if (valTemperature != null)
            mSensorManager.registerListener(mTemperatureListener,
                    valTemperature, SensorManager.SENSOR_DELAY_NORMAL)
        else
            temperature.setText(getString(R.string.unavailable))

    }


    override fun onPause() {
        super.onPause()
        mSensorManager.unregisterListener(mTemperatureListener)

        mTimer.cancel()

    }

    private fun updateUI(){
        requireActivity().runOnUiThread {
            if (!mCurrentTemperature.isNaN())
                temperature.setText(String.format(
                        getString(R.string.degree_celsius),
                        mCurrentTemperature)
                )
        }
    }

    private fun listAssociatedCachesToUser(user: User) {

        val results = if (myUser.isAdmin) {
            mRealm.where(GeoCache::class.java)
                    .equalTo("user.id", user.id)
                    .sort("id", Sort.ASCENDING).findAll()
        } else {
            mRealm.where(GeoCache::class.java)
                    .equalTo("user.id", user.id)
                    .equalTo("isApproved", true)
                    .sort("id", Sort.ASCENDING).findAll()
        }



        if (results.isNotEmpty()) {
            Toast.makeText(requireActivity(), "Caches found: ${results.size}", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(requireActivity(), resources.getString(R.string.noCachesFound), Toast.LENGTH_SHORT).show()
        }

        geo_cache_recycler_view.layoutManager = LinearLayoutManager(context)
        geo_cache_recycler_view.adapter = GeoCacheAdapter(results)
    }

    // Called once the fragment gets visible
    override fun onStart() {
        super.onStart()




        spinner.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(parentView: AdapterView<*>?, selectedItemView: View?, position: Int, id: Long) {
                if (position != 0) { // If user clicked on everything else but not 'My Caches', list that specific users' caches
                    val getUserOnClick = findUserWithId(position) // position = userId, because it is sorted in ascending order.

                    if (getUserOnClick != null) {
                        listAssociatedCachesToUser(getUserOnClick)
                    }
                } else { // user clicked on 'My Caches' get that caches associated with logged in user.
                    listAssociatedCachesToUser(myUser)
                }
            }

            override fun onNothingSelected(parentView: AdapterView<*>?) {
                // your code here
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.fragment_go_caching, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.scan_qr -> {
                val intent = Intent(activity, ScanQrActivity::class.java).apply {
                    putExtra("userId", myUser.id)
                }
                startActivity(intent)
                return true
            }
            R.id.add_cache -> {
                val manager = requireFragmentManager()
                val fragment = AddGeoCacheFragment()

                fragment.setUserId(this.userId)

                manager
                        .beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .addToBackStack(fragment_container?.tag.toString())
                        .commit()
                return true
            }
            R.id.edit_profile -> {
                val manager = requireFragmentManager()
                val fragment = EditProfileFragment()

                val myUser = mRealm.where(User::class.java)
                        .equalTo("id", this.userId).findFirst()

                if (myUser != null) {
                    fragment.setUserId(myUser.id)
                } else {
                    throw Resources.NotFoundException("Couldn't catch user ID") // this should not be happening
                }

                manager
                        .beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .addToBackStack(fragment_container?.tag.toString())
                        .commit()
                return true
            }
            R.id.log_out -> {
                // Creation of dialog
                val dialog = activity?.let {
                    AlertDialog.Builder(it).apply {
                        setTitle(resources.getString(R.string.areYouSureYouWantToLogOut))
                        setNegativeButton(resources.getString(R.string.no)) { dialoge, _ ->
                            dialoge.dismiss()

                        }
                        setPositiveButton(resources.getString(R.string.yes)) { dialoge, _ ->
                            val manager = requireFragmentManager()
                            val fragment = LoginFragment()
                            manager
                                    .beginTransaction()
                                    .replace(R.id.fragment_container, fragment)
                                    .addToBackStack(fragment_container?.tag.toString())
                                    .commit()
                            Toast.makeText(requireActivity(), resources.getString(R.string.loggedOut), Toast.LENGTH_SHORT).show()

                        }
                    }
                }
                dialog?.show()
                return true
            }

            else -> return super.onOptionsItemSelected(item)
        }
    }


    private inner class GeoCacheAdapter(data: OrderedRealmCollection<GeoCache>) :
            RealmRecyclerViewAdapter<GeoCache, GeoCacheAdapter.GeoCacheViewHolder>(data, true) {

        private var selectedPos = RecyclerView.NO_POSITION
        override fun onCreateViewHolder(parent: ViewGroup,
                                        viewType: Int): GeoCacheViewHolder {

            val layout = LayoutInflater.from(parent.context)
                    .inflate(R.layout.list_geo_cache, parent, false)
            return GeoCacheViewHolder(layout)
        }
        @SuppressLint("SetTextI18n")
        override fun onBindViewHolder(holder: GeoCacheViewHolder, position: Int) {
            val geoCache = getItem(position)
            holder.apply {
                cache?.text = geoCache?.cache
                where?.text = geoCache?.location
                category?.text = geoCache?.category
                createdDate?.text = geoCache?.createdDate
                updatedDate?.text = geoCache?.updatedDate
                userAssigned?.text = geoCache?.user?.firstName + " " + geoCache?.user?.lastName

                // decode the filePathFromDB into a Bitmap
                val bitmap: Bitmap = BitmapFactory.decodeFile(geoCache?.filePath)
                // Update imageView with the bitmap
                cachePhoto?.setImageBitmap(bitmap)



                itemView.isSelected = selectedPos == position
                itemView.setOnClickListener {
                    if (!myUser.isAdmin) {
                        // if user is not admin, check if user is allowed to edit their OWN cache
                        if (geoCache?.user?.id != myUser.id) {
                            Toast.makeText(requireActivity(), "You are not allowed to edit others Cache!", Toast.LENGTH_SHORT).show()
                            return@setOnClickListener
                        }
                    }
                    if(it != null){ // This is where the delete is performed when clicking on the recyclerview
                        val manager = requireFragmentManager()
                        val fragment = EditGeoCacheFragment()
                        fragment.setGeoCacheId(geoCache?.id!!)
                        fragment.setUserId(myUser.id)

                        val currentFragment = manager.findFragmentById((R.id.fragment_container))
                        manager
                                .beginTransaction()
                                .replace(R.id.fragment_container, fragment)
                                .addToBackStack(currentFragment?.tag)
                                .commit()
                    }
                    notifyItemChanged(selectedPos)
                    selectedPos = layoutPosition
                    notifyItemChanged(selectedPos)
                }
            }
        }
        internal inner class GeoCacheViewHolder(view: View) :
                RecyclerView.ViewHolder(view) {
            var cache: TextView? = view.findViewById(R.id.cache_text)
            var where: TextView? = view.findViewById(R.id.where_text)
            var category: TextView? = view.findViewById(R.id.category)
            val createdDate: TextView? = view.findViewById(R.id.createdDate_text)
            val updatedDate: TextView? = view.findViewById(R.id.updatedDate_text)
            val userAssigned: Chip? = view.findViewById(R.id.user_text)
            val cachePhoto: ImageView? = view.findViewById(R.id.cacheImage)
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        mRealm.close()
    }


    fun calcTotalPoints(user: User): Int? {
        var sum = 0
        user.geoCaches?.forEach { gc ->
            sum += categoryToPoints(gc.category)
        }
        return sum
    }

    private fun categoryToPoints(category: String): Int {
        println("### categoryToPoints called")
        var points = 0
        if (category == "Easy") points = 200
        if (category == "Medium") points = 500
        if (category == "Hard") points = 1000

        return points
    }

}

