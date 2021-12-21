package com.aitechnologies.utripod.ui.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.aitechnologies.utripod.databinding.ActivityCreateGroupBinding
import com.aitechnologies.utripod.interfaces.UploadFileResponse
import com.aitechnologies.utripod.repository.ChatsRepository
import com.aitechnologies.utripod.ui.viewModels.CreateGroupViewModel
import com.aitechnologies.utripod.ui.viewModels.CreateGroupViewModelProvider
import com.aitechnologies.utripod.util.AppSharedPreference.Companion.getUsername
import com.aitechnologies.utripod.util.AppUtil.Companion.dismissProgress
import com.aitechnologies.utripod.util.AppUtil.Companion.shortToast
import com.aitechnologies.utripod.util.AppUtil.Companion.showProgress
import com.aitechnologies.utripod.util.AppUtil.Companion.uploadFile
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import kotlinx.coroutines.ExperimentalCoroutinesApi

class CreateGroupActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCreateGroupBinding
    private lateinit var createGroupViewModel: CreateGroupViewModel
    private var uri: Uri? = null
    private var isUploaded = false
    private lateinit var imageResultLauncher: ActivityResultLauncher<Intent>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateGroupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val createGroupViewModelProvider =
            CreateGroupViewModelProvider(ChatsRepository(), application)

        createGroupViewModel = ViewModelProvider(
            this,
            createGroupViewModelProvider
        )[CreateGroupViewModel::class.java]

        imageResultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            if (it.resultCode == RESULT_OK) {
                uri = it.data!!.data
                createGroupViewModel.groups.imageUrl = "selected"
                Glide.with(this)
                    .load(uri)
                    .apply(RequestOptions.circleCropTransform())
                    .into(binding.imgImage)
            }
        }

        createGroupViewModel.isCreated.observe(this, {
            dismissProgress()
            if (it) {
                shortToast("Group created")
                onBackPressed()
            } else {
                shortToast("An error occurred")
            }
        })

        createGroupViewModel.validate.observe(this, @ExperimentalCoroutinesApi { event ->
            event.getContentIfNotHandled()?.let {
                when (it.data!!.response) {
                    0 -> shortToast(it.message!!)
                    1 -> binding.edtName.error = it.message
                    2 -> binding.edtDescription.error = it.message
                    3 -> shortToast(it.message!!)
                    4 -> {
                        showProgress("Creating group...", false)
                        if (isUploaded) {
                            createGroupViewModel.createGroup()
                        } else {
                            uploadFile(
                                uri!!,
                                createGroupViewModel.groups.roomId,
                                "group/${createGroupViewModel.groups.roomId}/",
                                object : UploadFileResponse {
                                    override fun onSuccess(filePath: String) {
                                        isUploaded = true
                                        createGroupViewModel.groups.imageUrl = filePath
                                        createGroupViewModel.createGroup()
                                    }

                                    override fun onProgress(progress: Int) {

                                    }

                                    override fun onFailure(message: String) {
                                        dismissProgress()
                                        shortToast(message)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        })

        binding.imgImage.setOnClickListener {
            imageResultLauncher.launch(Intent().apply {
                type = "image/*"
                action = Intent.ACTION_GET_CONTENT
            })
        }

        binding.txtCreate.setOnClickListener {
            createGroupViewModel.groups.name = binding.edtName.text.toString()
            createGroupViewModel.groups.description = binding.edtDescription.text.toString()
            createGroupViewModel.groups.admins = listOf(getUsername()).toString()

            createGroupViewModel.validate()
        }

    }
}