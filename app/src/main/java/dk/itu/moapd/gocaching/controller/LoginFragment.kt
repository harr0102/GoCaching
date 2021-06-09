package dk.itu.moapd.gocaching.controller


import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.*

import android.widget.Toast
import androidx.fragment.app.Fragment
import dk.itu.moapd.gocaching.R
import dk.itu.moapd.gocaching.model.User
import dk.itu.moapd.gocaching.view.GoCachingActivity
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_go_caching.*
import kotlinx.android.synthetic.main.fragment_login.*


class LoginFragment : Fragment() {
    private lateinit var mRealm: Realm

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)


    }

    private fun emailExists(email: String): Boolean {
        val userFound = mRealm.where(User::class.java)
                .equalTo("email", email).findFirst()

        if (userFound != null) {
            return true
        }

        return false
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_login, container, false)

        mRealm = Realm.getDefaultInstance()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.fragment_support, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.supportMenu -> {
                val manager = requireFragmentManager()
                val fragment = SupportFragment()
                manager
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(fragment_container?.tag.toString())
                    .commit()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    // Called once the fragment gets visible
    override fun onStart() {
        super.onStart()
        // Create admin if not already exists.
        if (!emailExists("admin")) {
            mRealm.executeTransactionAsync { realm ->
                var id = realm.where(User::class.java).max("id")
                if (id == null) id = 0


                val newUser = User(
                        id = id.toInt() + 1,
                        firstName = "Admin Harry",
                        lastName = "Admin Singh",
                        email = "admin",
                        password = "admin",
                        isAdmin = true
                )

                realm.insert(newUser)
            }
        }

        signUpButtonLogin.setOnClickListener {
            val manager = requireFragmentManager()
            val fragment = SignUpFragment()
            manager
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(fragment_container?.tag.toString())
                .commit()
        }

        loginButton.setOnClickListener {
            if(loginIsFilled()) {
                login()
            }
        }

    }


    @SuppressLint("ResourceType")
    private fun loginIsFilled(): Boolean {
        if (edit_text_loginEmail.text.isEmpty()) {
            Toast.makeText(requireActivity(), resources.getString(R.string.emailMustBeProvided), Toast.LENGTH_SHORT).show()
            return false
        }
        if (edit_text_loginPassword.text.isEmpty()) {
            Toast.makeText(requireActivity(), resources.getString(R.string.passwordMustBeProvided), Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    private fun login() {
        val typedEmail = edit_text_loginEmail.text.toString()
        val typedPassword = edit_text_loginPassword.text.toString()
        val userFound = mRealm.where(User::class.java)
            .equalTo("email", typedEmail).findFirst()

            if (userFound != null) {
                // User found through their email
                    // check if their email matches with the provided password
                if (userFound.password == typedPassword) {
                    val intent = Intent(activity, GoCachingActivity::class.java).apply {
                            putExtra("id", userFound.id)
                    }
                    startActivity(intent)
                    Toast.makeText(requireActivity(), "Logged in as ${userFound.firstName} ${userFound.lastName}", Toast.LENGTH_SHORT).show()
                } else
                {
                    Toast.makeText(requireActivity(), resources.getString(R.string.wrongPassword), Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(requireActivity(),  resources.getString(R.string.emailDoesntExists), Toast.LENGTH_SHORT).show()
            }
        }



    override fun onDestroy() {
        super.onDestroy()
        mRealm.close()
    }

}

