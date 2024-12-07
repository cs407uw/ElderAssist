package com.cs407.elderassist_tutorial


import android.content.Intent
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
    private lateinit var backToHomeButton: Button
    private lateinit var userViewModel: UserViewModel

    //private lateinit var userPasswdKV: SharedPreferences
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

        //userPasswdKV = requireContext().getSharedPreferences(getString(R.string.userPasswdKV), Context.MODE_PRIVATE)
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

        // 初始化按钮
        backToHomeButton = view.findViewById(R.id.backToHomeButton)

        // 设置点击事件
        backToHomeButton.setOnClickListener {
            val intent = Intent(requireContext(), HomeActivity::class.java)
            startActivity(intent)

            // 如果希望关闭当前 LoginActivity 页面，防止返回堆栈中残留：
            requireActivity().finish()
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
                    val user = withContext(Dispatchers.IO) {
                        val database = NoteDatabase.getDatabase(requireContext())
                        database.userDao().getUserByName(username)
                    }

                    if (user!=null&& validatePassword(password, user.passwd)) {
                        Log.d("LoginFragment", "Login successful for user: $username")
                        userViewModel.setUser(UserState(user.userId, username, password, user.randomInfo))
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


    private fun validatePassword(inputPassword: String, storedPassword: String): Boolean {
        return hash(inputPassword) == storedPassword
    }

    private fun hash(input: String): String {
        return MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
            .fold("") { str, it -> str + "%02x".format(it) }
    }
}