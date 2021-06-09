package dk.itu.moapd.gocaching.model

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.Required

open class User(
        @PrimaryKey var id: Int = 0,
        @Required var firstName: String = "",
        @Required var lastName: String = "",
        @Required var email: String = "",
        @Required var password: String = "",
        var geoCaches: RealmList<GeoCache> = RealmList(), // found GeoCaches
        var isAdmin: Boolean = false // default false

    ): RealmObject()

