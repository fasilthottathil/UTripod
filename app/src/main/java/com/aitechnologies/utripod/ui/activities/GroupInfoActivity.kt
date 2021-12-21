package com.aitechnologies.utripod.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.View.GONE
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.aitechnologies.utripod.R
import com.aitechnologies.utripod.adapters.UserAdapter
import com.aitechnologies.utripod.databinding.ActivityGroupInfoBinding
import com.aitechnologies.utripod.models.Groups
import com.aitechnologies.utripod.repository.ChatsRepository
import com.aitechnologies.utripod.repository.UserRepository
import com.aitechnologies.utripod.ui.viewModels.GroupInfoViewModel
import com.aitechnologies.utripod.ui.viewModels.GroupInfoViewModelProvider
import com.aitechnologies.utripod.util.AppSharedPreference.Companion.getUsername
import com.aitechnologies.utripod.util.AppUtil.Companion.dismissProgress
import com.aitechnologies.utripod.util.AppUtil.Companion.shortToast
import com.aitechnologies.utripod.util.AppUtil.Companion.showProgress
import com.bumptech.glide.Glide
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
class GroupInfoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityGroupInfoBinding
    private var groups = Groups()
    private val userAdapter by lazy { UserAdapter(this) }
    private lateinit var groupInfoViewModel: GroupInfoViewModel
    private lateinit var admins: List<String>
    private lateinit var updateGroupResultLauncher: ActivityResultLauncher<Intent>
    private var isEdited = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGroupInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        groups = intent.getBundleExtra("bundle")!!.getParcelable<Groups>("group") as Groups

        val groupInfoViewModelProvider = GroupInfoViewModelProvider(
            ChatsRepository(),
            UserRepository()
        )

        groupInfoViewModel = ViewModelProvider(
            this,
            groupInfoViewModelProvider
        )[GroupInfoViewModel::class.java]

        setUI()

        updateGroupResultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            if (it.resultCode == RESULT_OK) {
                isEdited = true
                groups =
                    it.data!!.getBundleExtra("bundle")!!.getParcelable<Groups>("group") as Groups
                setUI()
            }
        }

        groupInfoViewModel.getGroupMembers(groups.roomId)

        groupInfoViewModel.groupMembers.observe(this, { event ->
            event.getContentIfNotHandled()?.let {
                when (it.message) {
                    "error" -> hideLoading()
                    "success" -> {
                        it.data!!.add(admins[0])
                        groupInfoViewModel.getUsersByUsernameList(it.data)
                    }
                }
            }
        })

        groupInfoViewModel.users.observe(this, {
            hideLoading()
            binding.rvMembers.apply {
                setHasFixedSize(true)
                layoutManager = LinearLayoutManager(this@GroupInfoActivity)
                adapter = userAdapter
            }
            userAdapter.setData(it)
        })

        groupInfoViewModel.isCleared.observe(this, {
            dismissProgress()
            shortToast("Group messages cleared")
        })

        binding.txtAdd.setOnClickListener {
            startActivity(
                Intent(this, AddGroupMembersActivity::class.java)
                    .putExtra("roomId", groups.roomId)
            )
        }

        binding.txtEdit.setOnClickListener {
            updateGroupResultLauncher.launch(
                Intent(this, EditGroupActivity::class.java)
                    .putExtra("bundle", Bundle().apply { putParcelable("group", groups) })
            )
        }

        binding.txtShare.setOnClickListener {
            startActivity(Intent().apply {
                type = "text/plain"
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, "https://utripod.page.link/group/${groups.roomId}")
            })
        }

        binding.txtClear.setOnClickListener {
            AlertDialog.Builder(this)
                .apply {
                    setMessage("Do you want to delete all messages?")
                    setNegativeButton("No") { d, _ -> d.cancel() }
                    setPositiveButton("yes") { d, _ ->
                        d.cancel()
                        showProgress("Clearing...", false)
                        groupInfoViewModel.clearGroupMessages(groups.roomId)
                    }
                }.create().show()
        }

        userAdapter.setOnUserClickListener {
            if (it.username == getUsername()) {
                startActivity(Intent(this, MyProfileActivity::class.java))
            } else {
                startActivity(
                    Intent(this, OthersProfileActivity::class.java)
                        .putExtra("user", it)
                )
            }
        }

    }

    private fun hideLoading() {
        binding.progressCircular.visibility = GONE
    }

    private fun setUI() {
        Glide.with(applicationContext)
            .load(groups.imageUrl)
            .placeholder(R.drawable.image_place_holder)
            .error(R.drawable.image_place_holder)
            .into(binding.imgImage)

        binding.txtName.text = groups.name
        binding.txtDescription.text = groups.description
        "Members (${if (groups.members == 0) 1 else groups.members})"
            .also { binding.txtMembers.text = it }

        admins = groups.admins.replace("[", "")
            .replace("]", "")
            .split(",")
        var isAdmin = false
        admins.forEach {
            if (it == getUsername())
                isAdmin = true
        }

        if (!isAdmin) {
            binding.txtEdit.visibility = GONE
            binding.txtAdd.visibility = GONE
            binding.txtClear.visibility = GONE
        }

    }

    override fun onBackPressed() {
        if (isEdited) {
            setResult(
                RESULT_OK,
                Intent().putExtra("bundle", Bundle().apply { putParcelable("group", groups) })
            )
            finish()
        } else
            super.onBackPressed()
    }

}