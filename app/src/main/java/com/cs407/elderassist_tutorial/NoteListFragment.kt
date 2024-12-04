package com.cs407.elderassist_tutorial

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.cs407.elderassist_tutorial.data.NoteDatabase
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.util.Log
import android.widget.Button

class NoteListFragment(
    private val injectedUserViewModel: UserViewModel? = null
) : Fragment() {

    private lateinit var greetingTextView: TextView

    private lateinit var userViewModel: UserViewModel

    private lateinit var noteDB: NoteDatabase
    private lateinit var userPasswdKV: SharedPreferences

    private var deleteIt: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        noteDB = NoteDatabase.getDatabase(requireContext())
        userPasswdKV = requireContext().getSharedPreferences(
            getString(R.string.userPasswdKV), Context.MODE_PRIVATE)
        userViewModel = if (injectedUserViewModel != null) {
            injectedUserViewModel
        } else {
            ViewModelProvider(requireActivity()).get(UserViewModel::class.java)
        }

        val userName = userViewModel.userState.value?.name ?: ""
        Log.d("NoteListFragment", "Loaded userName: $userName")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val view = inflater.inflate(R.layout.fragment_note_list, container, false)
        greetingTextView = view.findViewById(R.id.greetingTextView)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        val menuHost = requireActivity()
//        menuHost.addMenuProvider(object : MenuProvider {
//            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
//                menuInflater.inflate(R.menu.note_list_menu, menu)
//            }
//
//            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
//                return when (menuItem.itemId) {
//                    R.id.action_logout -> {
//                        userViewModel.setUser(UserState())
//                        findNavController().navigate(R.id.action_noteListFragment_to_loginFragment)
//                        true
//                    }
//                    R.id.action_delete_account -> {
//                        deleteAccountAndLogout()
//                        true
//                    }
//                    else -> false
//                }
//            }
//        }, viewLifecycleOwner)
//
//        val userState = userViewModel.userState.value
//
//        greetingTextView.text = getString(R.string.greeting_text, userState.name)
        // 绑定按钮
        val logoutButton = view.findViewById<Button>(R.id.logoutButton)
        val deleteAccountButton = view.findViewById<Button>(R.id.deleteAccountButton)

        // 设置按钮点击事件
        logoutButton.setOnClickListener {
            // Log Out 的逻辑
            userViewModel.setUser(UserState())
            findNavController().navigate(R.id.action_noteListFragment_to_loginFragment)
        }

        deleteAccountButton.setOnClickListener {
            // Delete Account 的逻辑
            deleteAccountAndLogout()
        }

        // 设置欢迎文本
        val userState = userViewModel.userState.value
        greetingTextView.text = getString(R.string.greeting_text, userState.name)
    }

    private fun deleteAccountAndLogout() {
        val userName = userViewModel.userState.value?.name ?: ""

        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val user = noteDB.userDao().getByName(userName)
                    noteDB.deleteDao().delete(user.userId)
                    userPasswdKV.edit().remove(userName).apply()
                    userViewModel.setUser(UserState())

                    withContext(Dispatchers.Main) {
                        findNavController().navigate(R.id.action_noteListFragment_to_loginFragment)
                    }

                    Log.d("NoteListFragment", "User $userName successfully removed")
                } catch (e: Exception) {
                    Log.e("NoteListFragment", "Error deleting user: ${e.message}")
                }
            }
        }
    }
}
