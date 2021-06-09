package dk.itu.moapd.gocaching.controller


import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import dk.itu.moapd.gocaching.R
import dk.itu.moapd.gocaching.model.User
import io.realm.Realm
import kotlinx.android.synthetic.main.fragment_signup.*

class SignUpFragment : Fragment() {
    private lateinit var mRealm: Realm

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    private fun signUpIsFilled(): Boolean {
        if (edit_text_signUpFirstName.text.isEmpty()) {
            Toast.makeText(requireActivity(),  resources.getString(R.string.firstNameMustBeProvided), Toast.LENGTH_SHORT).show()
            return false
        }
        if (edit_text_signUpLastName.text.isEmpty()) {
            Toast.makeText(requireActivity(),  resources.getString(R.string.lastNameMustBeProvided), Toast.LENGTH_SHORT).show()
            return false
        }
        if (edit_text_signUpEmail.text.isEmpty()) {
            Toast.makeText(requireActivity(), resources.getString(R.string.emailMustBeProvided), Toast.LENGTH_SHORT).show()
            return false
        }
        if (edit_text_signUpPassword.text.isEmpty()) {
            Toast.makeText(requireActivity(), resources.getString(R.string.passwordMustBeProvided), Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    private fun emailExists(): Boolean {
        val userFound = mRealm.where(User::class.java)
            .equalTo("email", edit_text_signUpEmail.text.toString()).findFirst()

        if (userFound != null) {
            Toast.makeText(requireActivity(),resources.getString(R.string.emailAlreadyExists) , Toast.LENGTH_SHORT).show()
            return true
        }

        return false
    }

    private fun signUp() {
        val firstName = edit_text_signUpFirstName.text.toString()
        val lastName = edit_text_signUpLastName.text.toString()
        val email = edit_text_signUpEmail.text.toString()
        val password = edit_text_signUpPassword.text.toString()

        mRealm.executeTransactionAsync { realm ->
            var id = realm.where(User::class.java).max("id")
            if (id == null) id = 0


            val newUser = User(
                id = id.toInt() + 1,
                firstName = firstName,
                lastName = lastName,
                email = email,
                password = password
            )

            realm.insert(newUser)
        }

        Toast.makeText(requireActivity(), "User created with mail: $email", Toast.LENGTH_SHORT).show()

        val manager = requireFragmentManager()
        manager.popBackStack()
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_signup, container, false)

        mRealm = Realm.getDefaultInstance()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }


    // Called once the fragment gets visible
    override fun onStart() {
        super.onStart()

        signUpbutton.setOnClickListener {
            if (signUpIsFilled() && !emailExists()) {
                signUp()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mRealm.close()
    }

}

