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
import com.aitechnologies.utripod.adapters.PrivateMessageAdapter
import com.aitechnologies.utripod.databinding.ActivityPrivateChatBinding
import com.aitechnologies.utripod.interfaces.UploadFileResponse
import com.aitechnologies.utripod.models.Chats
import com.aitechnologies.utripod.repository.ChatsRepository
import com.aitechnologies.utripod.repository.NotificationRepository
import com.aitechnologies.utripod.ui.viewModels.PrivateChatViewModel
import com.aitechnologies.utripod.ui.viewModels.PrivateChatViewModelProvider
import com.aitechnologies.utripod.util.AppSharedPreference.Companion.getUsername
import com.aitechnologies.utripod.util.AppUtil
import com.aitechnologies.utripod.util.AppUtil.Companion.shortToast
import com.aitechnologies.utripod.util.AppUtil.Companion.showProgress
import com.vanniktech.emoji.EmojiManager
import com.vanniktech.emoji.EmojiPopup
import com.vanniktech.emoji.google.GoogleEmojiProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
class PrivateChatActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPrivateChatBinding
    private var chats = Chats()
    private val privateMessageAdapter by lazy { PrivateMessageAdapter(this) }
    private lateinit var imageResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var videoResultLauncher: ActivityResultLauncher<Intent>
    private var uri: Uri? = null
    private lateinit var privateChatViewModel: PrivateChatViewModel
    private lateinit var chatsRepository: ChatsRepository
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPrivateChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        chats = intent.getBundleExtra("bundle")!!.getParcelable<Chats>("chats") as Chats

        chatsRepository = ChatsRepository()
        val privateChatViewModelProvider = PrivateChatViewModelProvider(
            chatsRepository, application,
            NotificationRepository(application)
        )

        privateChatViewModel = ViewModelProvider(
            this,
            privateChatViewModelProvider
        )[PrivateChatViewModel::class.java]

        privateChatViewModel.getPrivateChatMessages(chats.roomId)

        privateChatViewModel.privateMessage.username = getUsername()

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
                            AppUtil.uploadFile(
                                uri!!,
                                System.currentTimeMillis().toString(),
                                "private/${chats.roomId}/images/",
                                object : UploadFileResponse {
                                    override fun onSuccess(filePath: String) {
                                        AppUtil.dismissProgress()
                                        privateChatViewModel.privateMessage.type = 1
                                        privateChatViewModel.privateMessage.message = filePath
                                        privateChatViewModel.validate()
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
                            AppUtil.uploadFile(
                                uri!!,
                                System.currentTimeMillis().toString(),
                                "private/${chats.roomId}/videos/",
                                object : UploadFileResponse {
                                    override fun onSuccess(filePath: String) {
                                        AppUtil.dismissProgress()
                                        privateChatViewModel.privateMessage.type = 2
                                        privateChatViewModel.privateMessage.message = filePath
                                        privateChatViewModel.validate()
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

        binding.txtName.text = chats.username

        privateChatViewModel.chatListener.observe(this, { event ->
            hideLoading()
            event.getContentIfNotHandled()?.let {
                when (it.message) {
                    "success" -> @SuppressWarnings("NotifyDataSetChanged") {
                        privateMessageAdapter.setData(it.data!!)
                        privateMessageAdapter.notifyDataSetChanged()
                        if (it.data.isNotEmpty())
                            binding.rvPrivateChats.scrollToPosition(it.data.size - 1)
                    }
                }
            }
        })

        privateChatViewModel.validate.observe(this, { event ->
            event.getContentIfNotHandled()?.let {
                when (it.data!!.response) {
                    1 -> shortToast(it.message!!)
                    2 -> {
                        binding.edtMesaage.setText("")
                        privateChatViewModel.sendPrivateChatMessage(chats.roomId, chats.username)
                    }
                }
            }
        })

        binding.imgSend.setOnClickListener {
            privateChatViewModel.privateMessage.message = binding.edtMesaage.text.toString()
            privateChatViewModel.privateMessage.type = 0
            privateChatViewModel.validate()
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

        privateMessageAdapter.setOnImageClickListener {
            startActivity(
                Intent(
                    this,
                    ViewImageActivity::class.java
                ).putExtra("image", it.message)
            )
        }

        privateMessageAdapter.setOnVideoClickListener {
            startActivity(
                Intent(
                    this,
                    ViewVideoActivity::class.java
                ).putExtra("video", it.message)
            )
        }

    }

    private fun setupRecyclerView() {
        binding.rvPrivateChats.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(this@PrivateChatActivity)
            adapter = privateMessageAdapter
        }
    }

    private fun hideLoading() {
        binding.progressCircular.visibility = INVISIBLE
    }

    private fun setupEmoji() {
        EmojiManager.install(GoogleEmojiProvider())
        val popup = EmojiPopup.Builder.fromRootView(findViewById(R.id.c)).build(binding.edtMesaage)
        binding.imgEmoji.setOnClickListener { popup.toggle() }
    }

    override fun onStop() {
        super.onStop()
        chatsRepository.markAsRead(getUsername(), chats.username)
    }

}