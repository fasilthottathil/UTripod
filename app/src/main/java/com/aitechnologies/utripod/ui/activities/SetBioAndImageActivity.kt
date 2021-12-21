package com.aitechnologies.utripod.ui.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.aitechnologies.utripod.databinding.ActivitySetBioAndImageBinding
import com.aitechnologies.utripod.interfaces.UploadFileResponse
import com.aitechnologies.utripod.repository.UserRepository
import com.aitechnologies.utripod.ui.viewModels.SetBioAndImageProvider
import com.aitechnologies.utripod.ui.viewModels.SetBioAndImageViewModel
import com.aitechnologies.utripod.util.AppSharedPreference.Companion.getProfileUrl
import com.aitechnologies.utripod.util.AppSharedPreference.Companion.getUsername
import com.aitechnologies.utripod.util.AppUtil.Companion.dismissProgress
import com.aitechnologies.utripod.util.AppUtil.Companion.isConnected
import com.aitechnologies.utripod.util.AppUtil.Companion.shortToast
import com.aitechnologies.utripod.util.AppUtil.Companion.showProgress
import com.aitechnologies.utripod.util.AppUtil.Companion.uploadFile
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

class SetBioAndImageActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySetBioAndImageBinding
    private lateinit var imageResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var setBioAndImageViewModel: SetBioAndImageViewModel
    private lateinit var setBioAndImageProvider: SetBioAndImageProvider
    private var uri: Uri? = null
    private var isUploaded = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySetBioAndImageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setBioAndImageProvider = SetBioAndImageProvider(UserRepository())
        setBioAndImageViewModel = ViewModelProvider(
            this,
            setBioAndImageProvider
        )[SetBioAndImageViewModel::class.java]

        imageResultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            if (it.resultCode == RESULT_OK) {
                isUploaded = false
                uri = it.data!!.data
                Glide.with(applicationContext)
                    .load(uri)
                    .apply(RequestOptions.circleCropTransform())
                    .into(binding.imgImage)
            }
        }

        setBioAndImageViewModel.isAdded.observe(this, {
            dismissProgress()
            if (it) {
                startActivity(
                    Intent(this, MainActivity::class.java)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                )
            } else {
                shortToast("An error occurred")
            }
        })

        binding.imgImage.setOnClickListener {
            imageResultLauncher.launch(Intent().apply {
                action = Intent.ACTION_GET_CONTENT
                type = "image/*"
            })
        }


        binding.txtUpdate.setOnClickListener {
            if (uri == null) {
                shortToast("Select image")
                return@setOnClickListener
            }
            if (binding.edtBio.text.isEmpty() || binding.edtBio.text.isBlank()) {
                binding.edtBio.error = "Enter bio"
                return@setOnClickListener
            }
            if (!isConnected()) {
                shortToast("No connection")
                return@setOnClickListener
            }

            showProgress("Loading...", false)
            setBioAndImage()
        }


    }

    private fun setBioAndImage() {
        if (isUploaded) {
            setBioAndImageViewModel.setBioAndImage(
                getUsername(),
                getProfileUrl(),
                binding.edtBio.text.toString()
            )
        } else {
            uploadFile(
                uri!!,
                getUsername(),
                "profile/",
                object : UploadFileResponse {
                    override fun onSuccess(filePath: String) {
                        setBioAndImageViewModel.setBioAndImage(
                            getUsername(),
                            getProfileUrl(),
                            binding.edtBio.text.toString()
                        )
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

    override fun onBackPressed() {
        AlertDialog.Builder(this).apply {
            setCancelable(false)
            setMessage("Do you want to skip?")
            setNegativeButton("No") { d, _ -> d.cancel() }
            setPositiveButton("Yes") { d, _ ->
                d.cancel()
                startActivity(
                    Intent(this@SetBioAndImageActivity, MainActivity::class.java)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                )
            }
        }.create().show()
    }

}