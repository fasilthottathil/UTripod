package com.aitechnologies.utripod.ui.activities

import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View.INVISIBLE
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.aitechnologies.utripod.R
import com.aitechnologies.utripod.adapters.GroupMessageAdapter
import com.aitechnologies.utripod.databinding.ActivityGroupChatBinding
import com.aitechnologies.utripod.interfaces.UploadFileResponse
import com.aitechnologies.utripod.models.Groups
import com.aitechnologies.utripod.repository.ChatsRepository
import com.aitechnologies.utripod.repository.UserRepository
import com.aitechnologies.utripod.ui.viewModels.GroupChatViewModel
import com.aitechnologies.utripod.ui.viewModels.GroupChatViewModelProvider
import com.aitechnologies.utripod.util.AppSharedPreference.Companion.getProfileUrl
import com.aitechnologies.utripod.util.AppSharedPreference.Companion.getUsername
import com.aitechnologies.utripod.util.AppUtil.Companion.dismissProgress
import com.aitechnologies.utripod.util.AppUtil.Companion.shortToast
import com.aitechnologies.utripod.util.AppUtil.Companion.showProgress
import com.aitechnologies.utripod.util.AppUtil.Companion.uploadFile
import com.vanniktech.emoji.EmojiManager
import com.vanniktech.emoji.EmojiPopup
import com.vanniktech.emoji.google.GoogleEmojiProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
class GroupChatActivity : AppCompatActivity() {
    private lateinit var binding: ActivityGroupChatBinding
    private lateinit var groupChatViewModel: GroupChatViewModel
    private val groupMessageAdapter by lazy { GroupMessageAdapter(applicationContext) }
    private var groups = Groups()
    private lateinit var imageResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var videoResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var groupInfoResultLauncher: ActivityResultLauncher<Intent>
    private var uri: Uri? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGroupChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val groupChatViewModelProvider =
            GroupChatViewModelProvider(ChatsRepository(), UserRepository(), application)

        groupChatViewModel = ViewModelProvider(
            this,
            groupChatViewModelProvider
        )[GroupChatViewModel::class.java]

        groups = intent.getBundleExtra("bundle")!!.getParcelable<Groups>("group") as Groups

        binding.txtName.text = groups.name

        groupChatViewModel.getGroupChatMessages(groups.roomId)

        groupChatViewModel.groupMessage.username = getUsername()
        groupChatViewModel.groupMessage.profileUrl = getProfileUrl()

        setupEmoji()

        setupRecyclerView()

        imageResultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            if (it.resultCode == RESULT_OK) {
                uri = it.data!!.data
                AlertDialog.Builder(this)
                    .apply {
                        setMessage("Do you want to send this image?")
                        setNegativeButton("no") { d, _ ->
                            d.cancel()
                        }
                        setPositiveButton("yes") { d, _ ->
                            d.cancel()
                            showProgress("Image sending...", false)
                            uploadFile(
                                uri!!,
                                System.currentTimeMillis().toString(),
                                "group/${groups.roomId}/images/",
                                object : UploadFileResponse {
                                    override fun onSuccess(filePath: String) {
                                        dismissProgress()
                                        groupChatViewModel.groupMessage.type = 1
                                        groupChatViewModel.groupMessage.message = filePath
                                        groupChatViewModel.validate()
                                    }

                                    override fun onProgress(progress: Int) {

                                    }

                                    override fun onFailure(message: String) {

                                    }
                                }
                            )
                        }
                    }.create()
                    .show()
            }
        }

        videoResultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            if (it.resultCode == RESULT_OK) {
                uri = it.data!!.data
                AlertDialog.Builder(this)
                    .apply {
                        setMessage("Do you want to send this video?")
                        setNegativeButton("no") { d, _ ->
                            d.cancel()
                        }
                        setPositiveButton("yes") { d, _ ->
                            d.cancel()
                            showProgress("Video sending...", false)
                            uploadFile(
                                uri!!,
                                System.currentTimeMillis().toString(),
                                "group/${groups.roomId}/videos/",
                                object : UploadFileResponse {
                                    override fun onSuccess(filePath: String) {
                                        dismissProgress()
                                        groupChatViewModel.groupMessage.type = 2
                                        groupChatViewModel.groupMessage.message = filePath
                                        groupChatViewModel.validate()
                                    }

                                    override fun onProgress(progress: Int) {

                                    }

                                    override fun onFailure(message: String) {

                                    }
                                }
                            )
                        }
                    }.create()
                    .show()
            }
        }

        groupInfoResultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            if (it.resultCode == RESULT_OK) {
                groups =
                    it.data!!.getBundleExtra("bundle")!!.getParcelable<Groups>("group") as Groups
                binding.txtName.text = groups.name
            }
        }

        groupChatViewModel.chatListener.observe(this, { event ->
            hideLoading()
            event.getContentIfNotHandled()?.let {
                when (it.message) {
                    "success" -> @SuppressWarnings("NotifyDataSetChanged") {
                        groupMessageAdapter.setData(it.data!!)
                        groupMessageAdapter.notifyDataSetChanged()
                        if (it.data.isNotEmpty())
                            binding.rvGroupMessages.scrollToPosition(it.data.size - 1)
                    }
                }
            }
        })

        groupChatViewModel.validate.observe(this, { event ->
            event.getContentIfNotHandled()?.let {
                when (it.data!!.response) {
                    1 -> shortToast(it.message!!)
                    2 -> {
                        binding.edtMesaage.setText("")
                        groupChatViewModel.sendGroupMessage(groups.roomId)
                    }
                }
            }
        })

        binding.imgSend.setOnClickListener {
            groupChatViewModel.groupMessage.message = binding.edtMesaage.text.toString()
            groupChatViewModel.groupMessage.type = 0
            groupChatViewModel.validate()
        }

        binding.imgGallery.setOnClickListener {
            AlertDialog.Builder(this)
                .apply {
                    setItems(arrayOf("Send image", "Send video"),
                        DialogInterface.OnClickListener { p0, p1 ->
                            p0!!.cancel()
                            if (p1 == 0) {
                                imageResultLauncher.launch(Intent().apply {
                                    action = Intent.ACTION_GET_CONTENT
                                    type = "image/*"
                                })
                            } else {
                                videoResultLauncher.launch(Intent().apply {
                                    action = Intent.ACTION_GET_CONTENT
                                    type = "video/*"
                                })
                            }
                        })
                }.create().show()
        }

        binding.imgInfo.setOnClickListener {
            groupInfoResultLauncher.launch(
                Intent(this, GroupInfoActivity::class.java)
                    .putExtra("bundle", Bundle().apply { putParcelable("group", groups) })
            )
        }

        groupMessageAdapter.setOnImageClickListener {
            startActivity(
                Intent(
                    this,
                    ViewImageActivity::class.java
                ).putExtra("image", it.message)
            )
        }

        groupMessageAdapter.setOnVideoClickListener {
            startActivity(
                Intent(
                    this,
                    ViewVideoActivity::class.java
                ).putExtra("video", it.message)
            )
        }

        groupMessageAdapter.setOnUsernameClickListener {
            showProgress("Loading...", false)
            groupChatViewModel.getUserProfile(it)
        }

        groupMessageAdapter.setOnProfileImageClickListener {
            showProgress("Loading...", false)
            groupChatViewModel.getUserProfile(it)
        }

        groupChatViewModel.userProfile.observe(this, @ExperimentalCoroutinesApi {
            dismissProgress()
            if (it[0].username == getUsername()) {
                startActivity(Intent(this, MyProfileActivity::class.java))
            } else {
                startActivity(
                    Intent(this, OthersProfileActivity::class.java)
                        .putExtra("user", it[0])
                )
            }
        })


    }

    private fun setupRecyclerView() {
        binding.rvGroupMessages.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(this@GroupChatActivity)
            adapter = groupMessageAdapter
        }
    }

    private fun setupEmoji() {
        EmojiManager.install(GoogleEmojiProvider())
        val popup = EmojiPopup.Builder.fromRootView(findViewById(R.id.c)).build(binding.edtMesaage)
        binding.imgEmoji.setOnClickListener { popup.toggle() }
    }

    private fun hideLoading() {
        binding.progressCircular.visibility = INVISIBLE
    }
}