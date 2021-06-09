package dk.itu.moapd.gocaching.view

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import dk.itu.moapd.gocaching.viewmodel.CameraVM
import dk.itu.moapd.gocaching.R
import dk.itu.moapd.gocaching.controller.CameraFragment

class CameraActivity : AppCompatActivity() {
    private val viewModel: CameraVM by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        setContentView(R.layout.activity_camera)

        val fragment =
            supportFragmentManager.findFragmentById(R.id.fragment)

        if (fragment == null)
            supportFragmentManager
                .beginTransaction()
                .add(R.id.fragment, CameraFragment())
                .commit()
    }

    // "Send text back" button click
   fun captureButton1(view: View) {
        val fm = supportFragmentManager

//if you added fragment via layout xml
        val fragment: CameraFragment? = fm.findFragmentById(R.id.fragment) as CameraFragment?
        fragment?.myMethod()
        // Get the text from the EditText
        val stringToPassBack = viewModel.getData()
        // Put the String to pass back into an Intent and close this activity
        val intent = Intent()
        intent.putExtra("keyName", stringToPassBack)
        setResult(Activity.RESULT_OK, intent)
        finish()

    }

}
