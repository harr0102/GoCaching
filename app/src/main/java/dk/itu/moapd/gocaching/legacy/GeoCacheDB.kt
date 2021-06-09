package dk.itu.moapd.gocaching.legacy
import android.content.Context
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

/*class  GeoCacheDB private  constructor(context: Context) {
    private  val  geoCaches = ArrayList <GeoCache>()
    private  var  lastCache = GeoCache("", "", "", "")

    init {
        // You  can  add  more  geocache  objects  if you  want to

        geoCaches.add(GeoCache("Chair", "ITU", "Created: " + TodayDate(), "")
        )
        geoCaches.add(GeoCache("Bike", "Fields", "Created: " + TodayDate(), "")
        )
        geoCaches.add(GeoCache("Ticket", "Kobenhavns  Lufthavn", "Created: " + TodayDate(), "")
        )

    }


    companion  object : GeoCacheDBHolder<GeoCacheDB, Context>(::GeoCacheDB)

    fun  getGeoCaches(): List <GeoCache> {
        return geoCaches
    }

    fun  addGeoCache(cache: String , where: String) {
        lastCache = GeoCache(cache, where, "Created: " + TodayDate(), "")
        geoCaches.add(lastCache)
    }

    fun deleteGeoCache(index: Int){
        geoCaches.removeAt(index)
    }

    fun  updateGeoCache(cache: String , where: String) {
        lastCache.cache = cache
        lastCache.where = where
        lastCache.updatedDate = "Updated: " + TodayDate()
    }

    fun  getLastGeoCacheInfo(): String {
        return lastCache.toString()
    }

    //Date is formatted to string
    private fun TodayDate(): String {
        val pattern = "HH:mm dd-MM-yyyy"
        val simpleDateFormat = SimpleDateFormat(pattern)
        val result = simpleDateFormat.format(Date())
        return result
    }
}

open class GeoCacheDBHolder <out T: Any , in A>( creator: (A) -> T) {
    private var  creator: ((A) -> T)? = creator

    @Volatile private var instance: T? = null

    fun get(arg: A): T {
        val checkInstance = instance
        if (checkInstance  != null)
            return  checkInstance

        return synchronized(this) {
            val checkInstanceAgain = instance
            if (checkInstanceAgain  != null)
                checkInstanceAgain
            else {
                val  created = creator !!(arg)
                instance = created
                creator = null
                created
            }
        }
    }
}*/

