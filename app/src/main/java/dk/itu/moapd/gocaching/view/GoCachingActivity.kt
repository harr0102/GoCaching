package dk.itu.moapd.gocaching.view

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import dk.itu.moapd.gocaching.R
import dk.itu.moapd.gocaching.app
import dk.itu.moapd.gocaching.controller.GoCachingFragment
import io.realm.Realm
import io.realm.RealmConfiguration
import io.realm.mongodb.App
import io.realm.mongodb.AppConfiguration
import io.realm.mongodb.Credentials
import io.realm.mongodb.User

class GoCachingActivity : AppCompatActivity() {

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_go_caching)

        var fragment = supportFragmentManager
            .findFragmentById(R.id.fragment_container)

        if (fragment == null) {
            val userId = intent.getIntExtra("id", -1)
            val bundle = Bundle().apply {
                putInt("id", userId)
            }

            fragment = GoCachingFragment().apply {
                arguments = bundle
            }

            supportFragmentManager
                    .beginTransaction()
                    .add(R.id.fragment_container, fragment)
                    .commit()
        }


        Realm.init(this)
        var config = RealmConfiguration.Builder()
                .deleteRealmIfMigrationNeeded()
                .allowQueriesOnUiThread(true)
                .allowWritesOnUiThread(true)
                .build()

        Realm.setDefaultConfiguration(config)
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

    private val SECOND_ACTIVITY_REQUEST_CODE = 0
    private var filePath: String = ""

    fun getFilePath(): String {
        return this.filePath
    }

    fun cameraButton1(view: View) {
        val intent = Intent(this, CameraActivity::class.java)
        startActivityForResult(intent, SECOND_ACTIVITY_REQUEST_CODE)
    }

    fun updateImageView(filePath: String) {

        // decode the filePath into a Bitmap
        val bitmap: Bitmap = BitmapFactory.decodeFile(filePath)

        // Update imageView with the bitmap
        val imageView = findViewById<ImageView>(R.id.photoImage)

        // set imageview: resets when rotating
        imageView?.setImageBitmap(bitmap)
    }
    // This method is called when the second activity finishes
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Check that it is the SecondActivity with an OK result
        if (requestCode == SECOND_ACTIVITY_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {

                // Get the filePath of the image:
                val returnString = data!!.getStringExtra("keyName")
                if (returnString != null) {
                    this.filePath = returnString
                    //updateImageView(returnString)
                }

            }
        }
    }
}

