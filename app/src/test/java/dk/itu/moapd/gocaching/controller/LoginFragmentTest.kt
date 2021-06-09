package dk.itu.moapd.gocaching.controller

import dk.itu.moapd.gocaching.model.User
import org.junit.Before

import org.junit.Assert.*
import org.junit.Test

class LoginFragmentTest {

    private lateinit var user: User
    private lateinit var user2: User
    private lateinit var subject: AddGeoCacheFragment
    @Before
    fun setUp() {
        subject = AddGeoCacheFragment()
        user = User(1,"FirstName", "LastName", "Email", "Password",false)
        user2 = User(2, "User2","User2", "User2", "User2", true )
    }

    @Test
    fun assertThatUserExists(){
        assertNotNull(user)
    }

    @Test
    fun assertThatUserIdIsCorrect(){
        assertEquals(user.id, 1)
    }

    @Test
    fun assertThatUserNameIsCorrect(){
        assertEquals(user.firstName, "FirstName")
        assertEquals(user.lastName, "LastName")
    }

    @Test
    fun assertThatPasswordIsCorrect(){
        assertEquals(user.password, "Password")
    }


}