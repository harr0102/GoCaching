package dk.itu.moapd.gocaching.model

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.Required

open class GeoCache(
    @PrimaryKey var id: Int = 0,
    @Required var cache: String = "",
    @Required var location: String = "",
    @Required var createdDate: String = "",
    @Required var updatedDate: String = "",
    @Required var category: String = "", // Easy, Medium, Hard
    @Required var filePath: String = "",
    @Required var isApproved: Boolean? = false,
    var user: User? = null,
    var lon: Double = 0.0,
    var lat: Double = 0.0
): RealmObject()
