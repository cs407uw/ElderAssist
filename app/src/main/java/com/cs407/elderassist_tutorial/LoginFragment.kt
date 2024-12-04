package com.cs407.elderassist_tutorial

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
import com.cs407.elderassist_tutorial.data.NoteDatabase
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
    private lateinit var signUpLinkTextView: TextView
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
        signUpLinkTextView = view.findViewById(R.id.signUpLinkTextView)
        errorTextView = view.findViewById(R.id.errorTextView)

        userViewModel = if (injectedUserViewModel != null) {
            injectedUserViewModel
        } else {
            ViewModelProvider(requireActivity()).get(UserViewModel::class.java)
        }
        //log
        Log.d("LoginFragment", "Login screen loaded")

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
            val username = usernameEditText.text.toString()
            val password = passwordEditText.text.toString()

            Log.d("LoginFragment", "Attempting login with username: $username")

            if (username.isEmpty() || password.isEmpty()) {
                errorTextView.text = "Username or Password cannot be empty."
                errorTextView.visibility = View.VISIBLE
            } else {
                lifecycleScope.launch {
                    val success = withContext(Dispatchers.IO) {
                        getUserPasswd(username, password)
                    }

                    if (success) {
                        Log.d("LoginFragment", "Login successful for user: $username")
                        userViewModel.setUser(UserState(noteDB.userDao().getByName(username).userId, username, password))
                        findNavController().navigate(R.id.action_loginFragment_to_noteListFragment)
                    } else {
                        Log.d("LoginFragment", getString(R.string.fail_login))
                        errorTextView.text = getString(R.string.fail_login)
                        errorTextView.visibility = View.VISIBLE
                    }
                }
            }
        }

        // Set the sign-up link click action
        signUpLinkTextView.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_signUpFragment)
        }
    }

    private suspend fun getUserPasswd(
        name: String,
        passwdPlain: String
    ): Boolean {
        val hashedPassword = hash(passwdPlain)
        return if (userPasswdKV.contains(name)) {
            val storedPassword = userPasswdKV.getString(name, "")
            storedPassword == hashedPassword
        } else {
            false
        }
    }

    private fun hash(input: String): String {
        return MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
            .fold("") { str, it -> str + "%02x".format(it) }
    }
}