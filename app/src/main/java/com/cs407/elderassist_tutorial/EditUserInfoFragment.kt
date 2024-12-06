package com.cs407.elderassist_tutorial

import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.cs407.elderassist_tutorial.data.NoteDatabase
import com.cs407.elderassist_tutorial.utils.parseUserInfo
import com.cs407.elderassist_tutorial.utils.generateUserInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.util.Log
import android.widget.TextView
import androidx.core.widget.doAfterTextChanged

class EditUserInfoFragment : Fragment() {

    private lateinit var usernameEditText: EditText
    private lateinit var ageEditText: EditText
    private lateinit var locationEditText: EditText
    private lateinit var preferencesEditText: EditText
    private lateinit var saveButton: Button
    private lateinit var errorTextView: TextView

    private lateinit var userViewModel: UserViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val view = inflater.inflate(R.layout.fragment_edit_user_info, container, false)

        usernameEditText = view.findViewById(R.id.usernameEditText)
        ageEditText = view.findViewById(R.id.ageEditText)
        locationEditText = view.findViewById(R.id.locationEditText)
        preferencesEditText = view.findViewById(R.id.preferencesEditText)
        saveButton = view.findViewById(R.id.saveUserInfoButton)
        errorTextView = view.findViewById(R.id.errorTextView)

        userViewModel = ViewModelProvider(requireActivity()).get(UserViewModel::class.java)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        usernameEditText.doAfterTextChanged {
            errorTextView.visibility = View.GONE
        }
        ageEditText.doAfterTextChanged {
            errorTextView.visibility = View.GONE
        }
        locationEditText.doAfterTextChanged {
            errorTextView.visibility = View.GONE
        }
        preferencesEditText.doAfterTextChanged {
            errorTextView.visibility = View.GONE
        }

        saveButton.setOnClickListener {
            val username = usernameEditText.text.toString().trim()
            val age = ageEditText.text.toString().toIntOrNull()
            val location = locationEditText.text.toString().trim()
            val preferences = preferencesEditText.text.toString().trim()

            if (username.isEmpty() || age==null || location.isEmpty() || preferences.isEmpty()) {
                errorTextView.text = "Please fill in all fields."
                errorTextView.visibility = View.VISIBLE
            } else {
                lifecycleScope.launch {
                    // 加载用户信息
                    saveUserInfo(username, age, location, preferences)
                    findNavController().navigate(R.id.action_editUserInfoFragment_to_noteListFragment)
                }
            }

        }

    }

    /**
     * 加载用户信息
     */
    private suspend fun loadUserInfo() {
        withContext(Dispatchers.IO) {
            val database = NoteDatabase.getDatabase(requireContext())
            Log.d("EditFragment", "3")
            val userState = userViewModel.userState.value
            userState?.let {
                val user = database.userDao().getUserById(it.id) // 查询用户信息
                Log.d("EditFragment", "4")
                user?.let { userInfo ->
                    // 解析 randomInfo
                    val parsedInfo = parseUserInfo(userInfo.randomInfo)
                    Log.d("EditFragment", "5")
                    withContext(Dispatchers.Main) {
                        // 设置 EditText 的值
                        usernameEditText.setText(parsedInfo.username)
                        ageEditText.setText(parsedInfo.age.toString())
                        locationEditText.setText(parsedInfo.location)
                        preferencesEditText.setText(parsedInfo.preferences.joinToString(", "))
                    }
                }
            }
        }
    }

    /**
     * 保存用户信息
     */
    private suspend fun saveUserInfo(username: String, age: Int, location: String, preferences: String) {
        withContext(Dispatchers.IO) {
            val database = NoteDatabase.getDatabase(requireContext())
            val userState = userViewModel.userState.value
            userState?.let {
                // 将用户输入的信息生成 JSON
                val randomInfo = generateUserInfo(username, age, location, preferences)
                database.userDao().updateRandomInfo(it.id, randomInfo) // 更新用户信息
                // 更新 ViewModel
                userViewModel.setUser(
                    UserState(it.id, it.name, it.passwd, randomInfo)
                )
            }
        }
    }
}