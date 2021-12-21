package com.aitechnologies.utripod.ui.activities

import android.os.Bundle
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.aitechnologies.utripod.adapters.UserAdapter
import com.aitechnologies.utripod.databinding.ActivityAddGroupMembersBinding
import com.aitechnologies.utripod.repository.ChatsRepository
import com.aitechnologies.utripod.repository.UserRepository
import com.aitechnologies.utripod.ui.viewModels.AddGroupMembersViewModel
import com.aitechnologies.utripod.ui.viewModels.AddGroupMembersViewModelProvider
import com.aitechnologies.utripod.util.AppUtil.Companion.dismissProgress
import com.aitechnologies.utripod.util.AppUtil.Companion.isConnected
import com.aitechnologies.utripod.util.AppUtil.Companion.shortToast
import com.aitechnologies.utripod.util.AppUtil.Companion.showProgress
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
class AddGroupMembersActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddGroupMembersBinding
    private lateinit var addGroupMembersViewModel: AddGroupMembersViewModel
    private val userAdapter by lazy { UserAdapter(this) }
    private var roomId = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddGroupMembersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val addGroupMembersViewModelProvider = AddGroupMembersViewModelProvider(
            ChatsRepository(),
            UserRepository()
        )

        roomId = intent.getStringExtra("roomId").toString()

        addGroupMembersViewModel = ViewModelProvider(
            this,
            addGroupMembersViewModelProvider
        )[AddGroupMembersViewModel::class.java]

        addGroupMembersViewModel.users.observe(this, {
            hideLoading()
            if (it.isEmpty()) {
                shortToast("no user found")
            } else {
                binding.rvUsers.apply {
                    setHasFixedSize(true)
                    layoutManager = LinearLayoutManager(this@AddGroupMembersActivity)
                    adapter = userAdapter
                }
                userAdapter.setData(it)
            }
        })

        addGroupMembersViewModel.isAdded.observe(this, { event ->
            dismissProgress()
            event.getContentIfNotHandled()?.let {
                when (it.message) {
                    "error" -> shortToast("An error occurred")
                    "success" -> shortToast("Added")
                    "exist" -> {
                        AlertDialog.Builder(this)
                            .apply {
                                setMessage(it.data)
                                setNegativeButton("Cancel") { d, _ ->
                                    d.cancel()
                                }
                            }.create().show()
                    }
                }
            }
        })

        userAdapter.setOnUserClickListener {
            AlertDialog.Builder(this)
                .apply {
                    setMessage("Do you want to add ${it.username} to your group?")
                    setNegativeButton("no") { d, _ -> d.cancel() }
                    setPositiveButton("yes") { d, _ ->
                        d.cancel()
                        showProgress("Adding...", false)
                        addGroupMembersViewModel.addGroupMember(
                            roomId,
                            it.username
                        )
                    }
                }.create().show()
        }

        binding.edtSearch.setOnEditorActionListener { _, i, _ ->
            val search = binding.edtSearch.text.toString()
            if (i == EditorInfo.IME_ACTION_SEARCH && search.isNotBlank() && search.isNotEmpty()) {
                if (!isConnected())
                    shortToast("No connection")
                else {
                    showLoading()
                    addGroupMembersViewModel.searchUser(search)
                }
            }
            true
        }

        binding.imgBack.setOnClickListener { onBackPressed() }

    }

    private fun showLoading() {
        binding.progressCircular.visibility = VISIBLE
    }

    private fun hideLoading() {
        binding.progressCircular.visibility = INVISIBLE
    }
}