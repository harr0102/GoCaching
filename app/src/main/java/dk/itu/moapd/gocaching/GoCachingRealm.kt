package dk.itu.moapd.gocaching

import android.app.Application
import android.util.Log
import io.realm.Realm
import io.realm.mongodb.App
import io.realm.mongodb.AppConfiguration
import io.realm.mongodb.Credentials
import io.realm.mongodb.User

lateinit var app: App
inline fun <reified T> T.TAG(): String = T::class.java.simpleName

class GoCachingRealm : Application() {
    override fun onCreate() {
        super.onCreate()

        Realm.init(this)


        // BuildConfig.MONGODB_REALM_APP_ID returns: Unresolved reference: BuildConfig
        // hardcode APP ID instead: realmserver-kpkii
        app = App(AppConfiguration.Builder("realmserver-kpkii").build())

        val anonymousCredentials: Credentials = Credentials.anonymous()
        var user: User?
        app.loginAsync(anonymousCredentials) {
            if (it.isSuccess) {
                Log.v("AUTH", "Successfully authenticated anonymously.")
                user = app.currentUser()
            } else {
                Log.e("AUTH", it.error.toString())
            }
        }
    }
}