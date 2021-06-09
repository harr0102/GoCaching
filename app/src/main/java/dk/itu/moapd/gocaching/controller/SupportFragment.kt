package dk.itu.moapd.gocaching.controller

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

import dk.itu.moapd.gocaching.R
import kotlinx.android.synthetic.main.activity_go_caching.*
import kotlinx.android.synthetic.main.fragment_login.*


class SupportFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_support, container, false)
    }


    override fun onStart() {
        super.onStart()

        signUpButtonLogin.setOnClickListener {
            val manager = requireFragmentManager()
            val fragment = LoginFragment()
            manager
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(fragment_container?.tag.toString())
                .commit()
        }

    }

}