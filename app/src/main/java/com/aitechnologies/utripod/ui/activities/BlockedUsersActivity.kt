package com.aitechnologies.utripod.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.View.INVISIBLE
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.aitechnologies.utripod.adapters.UserAdapter
import com.aitechnologies.utripod.databinding.ActivityBlockedUsersBinding
import com.aitechnologies.utripod.repository.UserRepository
import com.aitechnologies.utripod.ui.viewModels.BlockedUsersProvider
import com.aitechnologies.utripod.ui.viewModels.BlockedUsersViewModel
import com.aitechnologies.utripod.util.AppSharedPreference.Companion.getUsername
import com.aitechnologies.utripod.util.AppUtil.Companion.dismissProgress
import com.aitechnologies.utripod.util.AppUtil.Companion.shortToast
import com.aitechnologies.utripod.util.AppUtil.Companion.showProgress

class BlockedUsersActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBlockedUsersBinding
    private val userAdapter by lazy { UserAdapter(this) }
    private lateinit var blockedUsersViewModel: BlockedUsersViewModel
    private lateinit var blockedUsersProvider: BlockedUsersProvider
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBlockedUsersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setRecyclerView()

        blockedUsersProvider = BlockedUsersProvider(UserRepository())
        blockedUsersViewModel = ViewModelProvider(
            this,
            blockedUsersProvider
        )[BlockedUsersViewModel::class.java]

        blockedUsersViewModel.getBlockedUsers(getUsername())

        blockedUsersViewModel.blockedUsers.observe(this, {
            if (it.isNotEmpty()) {
                blockedUsersViewModel.getUserByIdList(it)
            } else {
                hideLoading()
                shortToast("No blocked users found")
            }
        })

        blockedUsersViewModel.users.observe(this, {
            hideLoading()
            userAdapter.setData(it)
        })

        blockedUsersViewModel.isUnblocked.observe(this, {
            dismissProgress()
            shortToast("Unblocked")
            startActivity(
                Intent(this, MainActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            )
        })

        userAdapter.setOnUserClickListener {
            AlertDialog.Builder(this).apply {
                setMessage("Do you want to unblock ${it.username} ?")
                setNegativeButton("No") { d, _ -> d.cancel() }
                setPositiveButton("Yes") { d, _ ->
                    d.cancel()
                    showProgress("Unblocking....", false)

                    blockedUsersViewModel.unblockUser(getUsername(), it.username)
                }
            }.create().show()
        }

        binding.imgBack.setOnClickListener { onBackPressed() }
    }

    private fun hideLoading() {
        binding.progressCircular.visibility = INVISIBLE
    }

    private fun setRecyclerView() {
        binding.rvUsers.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(this@BlockedUsersActivity)
            adapter = userAdapter
        }
    }
}