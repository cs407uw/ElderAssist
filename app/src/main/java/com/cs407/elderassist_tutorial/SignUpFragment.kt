package com.cs407.elderassist_tutorial

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.cs407.elderassist_tutorial.data.NoteDatabase
import com.cs407.elderassist_tutorial.data.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.security.MessageDigest
import android.util.Log
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.ViewModelProvider

class SignUpFragment : Fragment() {
    private lateinit var usernameEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var signUpButton: Button
    private lateinit var errorTextView: TextView

    private lateinit var userPasswdKV: SharedPreferences
    private lateinit var noteDB: NoteDatabase
    private lateinit var userViewModel: UserViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val view = inflater.inflate(R.layout.fragment_sign_up, container, false)

        // 初始化 UserViewModel
        userViewModel = ViewModelProvider(requireActivity()).get(UserViewModel::class.java)

        usernameEditText = view.findViewById(R.id.sigupusernameEditText)
        passwordEditText = view.findViewById(R.id.signuppasswordEditText)
        signUpButton = view.findViewById(R.id.signUpButton)
        errorTextView = view.findViewById(R.id.errorTextView)

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

        signUpButton.setOnClickListener {
            val username = usernameEditText.text.toString()
            val password = passwordEditText.text.toString()

            if (username.isEmpty() || password.isEmpty()) {
                errorTextView.text = "Username or Password cannot be empty."
                errorTextView.visibility = View.VISIBLE
            } else {
                lifecycleScope.launch {
                    val success = withContext(Dispatchers.IO) {
                        createUser(username, password)
                    }

                    if (success) {
                        Log.d("SignUpFragment", "Sign-up successful for user: $username")

                        // 设置 UserViewModel 的用户信息
                        val userId = noteDB.userDao().getByName(username).userId
                        userViewModel.setUser(UserState(userId, username, password))

                        findNavController().navigate(R.id.action_signUpFragment_to_loginFragment)
                    } else {
                        errorTextView.text = "User already exists or sign-up failed."
                        errorTextView.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    private suspend fun createUser(
        name: String,
        passwdPlain: String
    ): Boolean {
        val hashedPassword = hash(passwdPlain)

        return if (userPasswdKV.contains(name)) {
            false
        } else {
            val newUser = User(userName = name)
            noteDB.userDao().insert(newUser)
            userPasswdKV.edit()
                .putString(name, hashedPassword)
                .apply()
            true
        }
    }

    private fun hash(input: String): String {
        return MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
            .fold("") { str, it -> str + "%02x".format(it) }
    }
}