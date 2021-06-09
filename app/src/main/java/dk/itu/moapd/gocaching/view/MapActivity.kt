package dk.itu.moapd.gocaching.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import dk.itu.moapd.gocaching.R
import dk.itu.moapd.gocaching.MapFragment


class MapActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        var fragment = supportFragmentManager
            .findFragmentById(R.id.fragment)

        if (fragment == null) {
            val longitude = intent.getDoubleExtra("longitude", 0.0)
            val latitude = intent.getDoubleExtra("latitude", 0.0)

            val bundle = Bundle().apply {
                putDouble("longitude", longitude)
                putDouble("latitude", latitude)
            }

            fragment = MapFragment().apply {
                arguments = bundle
            }

            supportFragmentManager
                .beginTransaction()
                .add(R.id.fragment, fragment)
                .commit()
        }
    }

}