package com.aitechnologies.utripod.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.View.INVISIBLE
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.aitechnologies.utripod.R
import com.aitechnologies.utripod.databinding.ActivityAppLinkBinding
import com.aitechnologies.utripod.models.Groups
import com.aitechnologies.utripod.repository.ChatsRepository
import com.aitechnologies.utripod.repository.UserRepository
import com.aitechnologies.utripod.ui.viewModels.AppLinkViewModel
import com.aitechnologies.utripod.ui.viewModels.AppLinkViewModelProvider
import com.aitechnologies.utripod.util.AppSharedPreference.Companion.getUsername
import com.aitechnologies.utripod.util.AppSharedPreference.Companion.isLogin
import com.aitechnologies.utripod.util.AppUtil
import com.aitechnologies.utripod.util.AppUtil.Companion.shortToast
import com.aitechnologies.utripod.util.AppUtil.Companion.showProgress
import com.aitechnologies.utripod.uvis.activities.UvisSingleActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
class AppLinkActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAppLinkBinding
    private var list: List<String> = arrayListOf()
    private var myArrayList = arrayListOf<String>()
    private var groups = Groups()
    private var isJoined = false
    private var groupId = ""
    private lateinit var appLinkViewModel: AppLinkViewModel
    private lateinit var appLinkViewModelProvider: AppLinkViewModelProvider
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAppLinkBinding.inflate(layoutInflater)
        setContentView(binding.root)

        list = intent.data.toString().split("https://utripod.page.link/")

        if (!isLogin()) {
            startActivity(Intent(this, SplashActivity::class.java))
            finish()
        }

        appLinkViewModelProvider = AppLinkViewModelProvider(ChatsRepository(), UserRepository())
        appLinkViewModel = ViewModelProvider(
            this,
            appLinkViewModelProvider
        )[AppLinkViewModel::class.java]

        if (list.isNotEmpty()) {
            list.forEach {
                if (it.isNotEmpty() && it.isNotBlank()) {
                    val list = it.split("/")
                    list.forEach { split ->
                        if (split.isNotEmpty() && split.isNotBlank()) {
                            myArrayList.add(split)
                        }
                    }

                }
            }
            if (myArrayList.size < 2) {
                shortToast("Corrupted link")
            } else {
                when (myArrayList[0]) {
                    "posts" -> {
                        startActivity(
                            Intent(this, PostSingleActivity::class.java)
                                .putExtra("id", myArrayList[1])
                        )
                    }
                    "uvis" -> {
                        startActivity(
                            Intent(this, UvisSingleActivity::class.java)
                                .putExtra("id", myArrayList[1])
                        )
                    }
                    "group" -> {
                        groupId = myArrayList[1]
                        appLinkViewModel.getJoinedGroupsIdList(getUsername())
                    }
                    "user"->{
                        appLinkViewModel.getUserProfile(myArrayList[1])
                    }
                }
            }
        } else {
            shortToast("Corrupted link")
        }

        appLinkViewModel.user.observe(this,{
            if (it[0].username == getUsername()) {
                startActivity(Intent(this, MyProfileActivity::class.java))
            } else {
                startActivity(
                    Intent(this, OthersProfileActivity::class.java)
                        .putExtra("user", it[0])
                )
            }
        })

        appLinkViewModel.joinedGroups.observe(this, { event ->
            event.getContentIfNotHandled()?.let {
                when (it.message) {
                    "error" -> {
                        shortToast("An error occurred")
                        onBackPressed()
                    }
                    "success" -> {
                        if (it.data!!.contains(groupId))
                            isJoined = true
                        appLinkViewModel.getGroupById(groupId)
                    }
                }
            }
        })

        appLinkViewModel.group.observe(this, { event ->
            hideLoading()
            event.getContentIfNotHandled()?.let {
                when (it.message) {
                    "error" -> {
                        shortToast("Group not found!")
                        onBackPressed()
                    }
                    "success" -> {
                        groups = it.data!![0]
                        showBottomSheet()
                    }
                }
            }
        })

        appLinkViewModel.isJoined.observe(this, { event ->
            AppUtil.dismissProgress()
            event.getContentIfNotHandled()?.let {
                when (it.message) {
                    "error" -> {
                        shortToast("An error occurred")
                        onBackPressed()
                    }
                    "success" -> {
                        shortToast("Joined")
                        startActivity(
                            Intent(this, GroupChatActivity::class.java)
                                .putExtra(
                                    "bundle",
                                    Bundle().apply { putParcelable("group", groups) })
                        )
                    }
                    "exist" -> {
                        AlertDialog.Builder(this)
                            .apply {
                                setCancelable(false)
                                setMessage(it.data)
                                setNegativeButton("Cancel") { d, _ ->
                                    d.cancel()
                                    startActivity(
                                        Intent(this@AppLinkActivity, GroupChatActivity::class.java)
                                            .putExtra(
                                                "bundle",
                                                Bundle().apply { putParcelable("group", groups) })
                                    )
                                }
                            }.create().show()
                    }
                }
            }
        })

    }

    private fun hideLoading() {
        binding.progressCircular.visibility = INVISIBLE
    }

    private fun showBottomSheet() {
        val view = View.inflate(this, R.layout.group_bottom_sheet, null)
        val imgImage = view.findViewById<ImageView>(R.id.imgImage)
        val txtName = view.findViewById<TextView>(R.id.txtName)
        val txtDescription = view.findViewById<TextView>(R.id.txtDescription)
        val txtJoin = view.findViewById<TextView>(R.id.txtJoin)

        Glide.with(applicationContext)
            .load(groups.imageUrl)
            .apply(RequestOptions.circleCropTransform())
            .into(imgImage)

        txtName.text = groups.name
        txtDescription.text = groups.description

        if (isJoined)
            "Open group".also { txtJoin.text = it }


        val bottomSheetDialog = BottomSheetDialog(this, R.style.bottom_sheet_dialog_theme)
        bottomSheetDialog.setContentView(view)
        bottomSheetDialog.show()

        bottomSheetDialog.setOnCancelListener {
            onBackPressed()
        }

        txtJoin.setOnClickListener {
            if (txtJoin.text == "Join") {
                showProgress("Joining...", false)
                appLinkViewModel.joinGroup(groups.roomId, getUsername())
            } else {
                startActivity(
                    Intent(this, GroupChatActivity::class.java)
                        .putExtra("bundle", Bundle().apply { putParcelable("group", groups) })
                )
            }
        }

    }

    override fun onBackPressed() {
        startActivity(Intent(this, SplashActivity::class.java))
    }

}