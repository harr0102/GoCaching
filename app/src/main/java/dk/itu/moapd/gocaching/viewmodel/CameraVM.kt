package dk.itu.moapd.gocaching.viewmodel

import androidx.lifecycle.ViewModel
import java.io.File

class CameraVM : ViewModel() {
    private var data: String? = null

    fun setData(data: String?) {
        this.data = data
    }

    fun getData(): String? {
        return this.data
    }
}