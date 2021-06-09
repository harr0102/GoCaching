package dk.itu.moapd.gocaching.controller


import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import dk.itu.moapd.gocaching.R
import dk.itu.moapd.gocaching.model.User
import dk.itu.moapd.gocaching.view.GoCachingActivity
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_go_caching.*
import kotlinx.android.synthetic.main.fragment_edit_profile.*
import kotlinx.android.synthetic.main.fragment_geo_cache.*
import kotlinx.android.synthetic.main.fragment_login.*
import java.util.*

class EditProfileFragment : Fragment() {

    private lateinit var mRealm: Realm
    private lateinit var myUser: User
    private lateinit var mPreferences: SharedPreferences

    private var hasRotated = false
    private var myUserId = -1

    companion object
    {
        private const val USER_ID = "user_id"
        private const val ROTATED = "landscape"
    }

    fun setUserId(myUserId: Int) {
        this.myUserId = myUserId
    }

    private fun findUserWithId(userId: Int): User? {
        return mRealm.where(User::class.java)
                .equalTo("id", userId).findFirst()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mRealm = Realm.getDefaultInstance()
        mPreferences = activity?.getPreferences(Context.MODE_PRIVATE)!!

        if (savedInstanceState != null) {
            this.myUserId = savedInstanceState.getInt(USER_ID, -1)
            this.hasRotated = savedInstanceState.getBoolean(ROTATED, false)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {

        outState.putInt(USER_ID, this.myUserId)
        outState.putBoolean(ROTATED, true)
        super.onSaveInstanceState(outState)

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_edit_profile, container, false)
        setHasOptionsMenu(true)

        mRealm = Realm.getDefaultInstance()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val myUser = findUserWithId(this.myUserId)
        if (myUser != null) {
            this.myUser = myUser
        } else {
            throw Resources.NotFoundException("Couldn't catch user ID") // this should not be happening
        }

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.fragment_edit_profile, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.delete_user -> {
                val dialog = activity?.let {
                    AlertDialog.Builder(it).apply {
                        setTitle("Are you sure you want to delete ${myUser.email}?")
                        setNegativeButton(resources.getString(R.string.no)) { dialoge, _ ->
                            dialoge.dismiss()

                        }
                        setPositiveButton(resources.getString(R.string.yes)) { dialoge, _ ->
                            deleteUser(myUser)
                            val manager = requireFragmentManager()
                            val fragment = LoginFragment()
                            manager
                                    .beginTransaction()
                                    .replace(R.id.fragment_container, fragment)
                                    .addToBackStack(fragment_container?.tag.toString())
                                    .commit()
                            Toast.makeText(requireActivity(), resources.getString(R.string.yourUserHasNowBeenDeleted), Toast.LENGTH_SHORT).show()

                        }
                    }
                }
                dialog?.show()
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun deleteUser(user: User)  {
        val id = user.id

        mRealm.executeTransactionAsync { realm ->
            val userFromDB = realm.where(User::class.java)
                    .equalTo("id", id).findFirst()
            userFromDB?.deleteFromRealm()
        }
    }
    // Called once the fragment gets visible
    override fun onStart() {
        super.onStart()
        if (!this.hasRotated) {
            profileFirstName.setText(this.myUser.firstName)
            profileLastName.setText(this.myUser.lastName)
            profileEmail.setText(this.myUser.email)
            profilePassword.setText(this.myUser.password)
        }


        updateBTN.setOnClickListener {
            val id = if (myUser != null) myUser?.id else -1

            if (profileEmail.text.isNotEmpty() && profileEmail.text.toString() != this.myUser.email) {
                // check if email is already registered - meaning that the user cannot change email to an already existing email
                val userFound = mRealm.where(User::class.java)
                    .equalTo("email", profileEmail.text.toString()).findFirst()

                if (userFound != null) {
                    Toast.makeText(requireActivity(), resources.getString(R.string.emailIsAlreadyInUse), Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
            }


            mRealm.executeTransactionAsync { realm ->
                val userFromDB = realm.where(User::class.java)
                    .equalTo("id", id).findFirst()

                    userFromDB?.firstName = profileFirstName.text.toString()
                    userFromDB?.lastName = profileLastName.text.toString()
                    userFromDB?.email = profileEmail.text.toString()
                    userFromDB?.password = profilePassword.text.toString()
            }

            Toast.makeText(requireActivity(), resources.getString(R.string.successfullyUpdated), Toast.LENGTH_SHORT).show()
            val manager = requireFragmentManager()
            manager.popBackStack()
        }
    }






    override fun onDestroy() {
        super.onDestroy()
        mRealm.close()
    }

}

