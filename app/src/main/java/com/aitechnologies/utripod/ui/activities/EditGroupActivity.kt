package com.aitechnologies.utripod.ui.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.aitechnologies.utripod.databinding.ActivityEditGroupBinding
import com.aitechnologies.utripod.interfaces.UploadFileResponse
import com.aitechnologies.utripod.models.Groups
import com.aitechnologies.utripod.repository.ChatsRepository
import com.aitechnologies.utripod.ui.viewModels.EditGroupViewModel
import com.aitechnologies.utripod.ui.viewModels.EditGroupViewModelProvider
import com.aitechnologies.utripod.util.AppUtil.Companion.dismissProgress
import com.aitechnologies.utripod.util.AppUtil.Companion.shortToast
import com.aitechnologies.utripod.util.AppUtil.Companion.showProgress
import com.aitechnologies.utripod.util.AppUtil.Companion.uploadFile
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

class EditGroupActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditGroupBinding
    private lateinit var imageResultLauncher: ActivityResultLauncher<Intent>
    private var uri: Uri? = null
    private var isUploaded = false
    private lateinit var editGroupViewModel: EditGroupViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditGroupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val editGroupViewModelProvider = EditGroupViewModelProvider(
            ChatsRepository(),
            application
        )

        editGroupViewModel = ViewModelProvider(
            this,
            editGroupViewModelProvider
        )[EditGroupViewModel::class.java]

        editGroupViewModel.groups =
            intent.getBundleExtra("bundle")!!.getParcelable<Groups>("group") as Groups

        setUI()

        editGroupViewModel.isUpdated.observe(this, {
            dismissProgress()
            shortToast("Updated")
            setResult(
                RESULT_OK,
                Intent().putExtra(
                    "bundle",
                    Bundle().apply { putParcelable("group", editGroupViewModel.groups) })
            )
            finish()
        })

        editGroupViewModel.validate.observe(this, { event ->
            event.getContentIfNotHandled()?.let {
                when (it.data!!.response) {
                    0 -> binding.edtName.error = it.message
                    1 -> binding.edtDescription.error = it.message
                    2 -> shortToast(it.message!!)
                    3 -> {
                        showProgress("Updating...", false)
                        if (uri != null) {
                            if (isUploaded) {
                                editGroupViewModel.editGroup(editGroupViewModel.groups.roomId)
                            } else {
                                uploadFile(
                                    uri!!,
                                    editGroupViewModel.groups.roomId,
                                    "groups/${editGroupViewModel.groups.roomId}",
                                    object : UploadFileResponse {
                                        override fun onSuccess(filePath: String) {
                                            editGroupViewModel.groups.imageUrl = filePath
                                            editGroupViewModel.editGroup(editGroupViewModel.groups.roomId)
                                        }

                                        override fun onProgress(progress: Int) {

                                        }

                                        override fun onFailure(message: String) {
                                            shortToast(message)
                                            dismissProgress()
                                        }
                                    }
                                )
                            }
                        } else {
                            editGroupViewModel.editGroup(editGroupViewModel.groups.roomId)
                        }
                    }
                }
            }
        })

        imageResultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            if (it.resultCode == RESULT_OK) {
                uri = it.data!!.data
                Glide.with(this)
                    .load(uri)
                    .apply(RequestOptions.circleCropTransform())
                    .into(binding.imgImage)
            }
        }

        binding.imgImage.setOnClickListener {
            imageResultLauncher.launch(
                Intent().apply {
                    action = Intent.ACTION_GET_CONTENT
                    type = "image/*"
                }
            )
        }

        binding.txtUpdate.setOnClickListener {
            editGroupViewModel.groups.name = binding.edtName.text.toString()
            editGroupViewModel.groups.description = binding.edtDescription.text.toString()

            editGroupViewModel.validate()
        }


    }

    private fun setUI() {
        Glide.with(applicationContext)
            .load(editGroupViewModel.groups.imageUrl)
            .apply(RequestOptions.circleCropTransform())
            .into(binding.imgImage)
        binding.edtName.setText(editGroupViewModel.groups.name)
        binding.edtDescription.setText(editGroupViewModel.groups.description)
    }
}