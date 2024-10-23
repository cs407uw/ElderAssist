package com.cs407.lab5_milestone
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.cs407.lab5_milestone.data.NoteDatabase
import com.cs407.lab5_milestone.data.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.security.MessageDigest
import android.util.Log

class LoginFragment(
    private val injectedUserViewModel: UserViewModel? = null // For testing only
) : Fragment() {

    private lateinit var usernameEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var errorTextView: TextView

    private lateinit var userViewModel: UserViewModel

    private lateinit var userPasswdKV: SharedPreferences
    private lateinit var noteDB: NoteDatabase

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val view = inflater.inflate(R.layout.fragment_login, container, false)

        usernameEditText = view.findViewById(R.id.usernameEditText)
        passwordEditText = view.findViewById(R.id.passwordEditText)
        loginButton = view.findViewById(R.id.loginButton)
        errorTextView = view.findViewById(R.id.errorTextView)

        userViewModel = if (injectedUserViewModel != null) {
            injectedUserViewModel
        } else {
            // TODO - Use ViewModelProvider to init UserViewModel
            //UserViewModel()
            ViewModelProvider(requireActivity()).get(UserViewModel::class.java)
        }
        //log
        Log.d("LoginFragment", "Login screen loaded")

        // TODO - Get shared preferences from using R.string.userPasswdKV as the name
        userPasswdKV = requireContext().getSharedPreferences(getString(R.string.userPasswdKV), Context.MODE_PRIVATE)
        noteDB = NoteDatabase.getDatabase(requireContext())

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        usernameEditText.doAfterTextChanged {
            errorTextView.visibility = View.GONE
        }

        passwordEditText.doAfterTextChanged {
            errorTextView.visibility = View.GONE
        }

        // Set the login button click action
        loginButton.setOnClickListener {
            // TODO: Get the entered username and password from EditText fields
            val username = usernameEditText.text.toString()
            val password = passwordEditText.text.toString()

            //log
            Log.d("LoginFragment", "Attempting login with username: $username and password: $password")

            // TODO: Set the logged-in user in the ViewModel (store user info) (placeholder)
            //userViewModel.setUser(UserState(0, "name", "passwd")) // You will implement this in UserViewModel
            if (username.isEmpty() || password.isEmpty()) {
                errorTextView.text = "Username or Password cannot be empty."
                errorTextView.visibility = View.VISIBLE
            }else{
                //Log.d("LoginFragment", "1")
            // TODO: Navigate to another fragment after successful login
            //findNavController().navigate(R.id.action_loginFragment_to_noteListFragment) // Example navigation action
            // TODO: Show an error message if either username or password is empty
            //errorTextView.visibility = View.VISIBLE
                lifecycleScope.launch {
                    val success = withContext(Dispatchers.IO) {
                        getUserPasswd(username, password)
                    }

                    if (success) {
                        //log
                        Log.d("LoginFragment", "Login successful for user: $username")
                        //logSharedPreferences()

                        // Set user in ViewModel and navigate to note list
                        //Log.d("LoginFragment", "Current UserState after setUser: ${currentUser?.name}")
                        userViewModel.setUser(UserState(0, username, password))
                        findNavController().navigate(R.id.action_loginFragment_to_noteListFragment)
                    } else {
                        //log
                        Log.d("LoginFragment", getString(R.string.fail_login))

                        errorTextView.text = "Login failed. Incorrect username or password."
                        errorTextView.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    //Check
//    private fun logSharedPreferences() {
//        val allEntries = userPasswdKV.all
//        for ((key, value) in allEntries) {
//            Log.d("SharedPreferences", "Key: $key, Value: $value")
//        }
//    }

    private suspend fun getUserPasswd(
        name: String,
        passwdPlain: String
    ): Boolean {
        // TODO: Hash the plain password using a secure hashing function

        val hashedPassword = hash(passwdPlain)

        // TODO: Check if the user exists in SharedPreferences (using the username as the key)
        // Check if the user exists in SharedPreferences
        return if (userPasswdKV.contains(name)) {

            val storedPassword = userPasswdKV.getString(name, "")
            Log.d("LoginFragment", "storedPassword: $storedPassword")
            if (storedPassword == hashedPassword) {
                // The user exists and the password is correct, proceed with login
                true
            } else {
                // The user exists but the password is incorrect, display an error
                false
            }
        } else {
            //val newUser = User(userId=passwdPlain.toInt(),userName = name)
            //noteDB.userDao().insert(newUser)
            userPasswdKV.edit()
                .putString(name, hashedPassword)
                .apply()

            true
        }
        // TODO: Retrieve the stored password from SharedPreferences

        // TODO: Compare the hashed password with the stored one and return false if they don't match

        // TODO: If the user doesn't exist in SharedPreferences, create a new user

        // TODO: Insert the new user into the Room database (implement this in your User DAO)

        // TODO: Store the hashed password in SharedPreferences for future logins

        // TODO: Return true if the user login is successful or the user was newly created

        //return true
    }

    private fun hash(input: String): String {
        return MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
            .fold("") { str, it -> str + "%02x".format(it) }
    }
}