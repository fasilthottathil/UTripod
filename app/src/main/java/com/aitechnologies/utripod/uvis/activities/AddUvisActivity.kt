package com.aitechnologies.utripod.uvis.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.aitechnologies.utripod.databinding.ActivityAddUvisBinding
import com.aitechnologies.utripod.interfaces.UploadFileResponse
import com.aitechnologies.utripod.repository.UvisRepository
import com.aitechnologies.utripod.ui.activities.MainActivity
import com.aitechnologies.utripod.ui.activities.PromotePostActivity
import com.aitechnologies.utripod.util.AppSharedPreference.Companion.getProfession
import com.aitechnologies.utripod.util.AppSharedPreference.Companion.getProfileUrl
import com.aitechnologies.utripod.util.AppSharedPreference.Companion.getUsername
import com.aitechnologies.utripod.util.AppUtil
import com.aitechnologies.utripod.util.AppUtil.Companion.dismissProgress
import com.aitechnologies.utripod.util.AppUtil.Companion.shortToast
import com.aitechnologies.utripod.util.AppUtil.Companion.showProgress
import com.aitechnologies.utripod.util.AppUtil.Companion.uploadFile
import com.aitechnologies.utripod.uvis.viewModels.AddUvisViewModel
import com.aitechnologies.utripod.uvis.viewModels.AddUvisViewModelProvider
import java.io.File

class AddUvisActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddUvisBinding
    private lateinit var addUvisViewModel: AddUvisViewModel
    private lateinit var addUvisViewModelProvider: AddUvisViewModelProvider
    private var isUploaded = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddUvisBinding.inflate(layoutInflater)
        setContentView(binding.root)

        addUvisViewModelProvider = AddUvisViewModelProvider(
            UvisRepository(application),
            application
        )

        addUvisViewModel = ViewModelProvider(
            this,
            addUvisViewModelProvider
        )[AddUvisViewModel::class.java]

        addUvisViewModel.validate.observe(this, { event ->
            event.getContentIfNotHandled()?.let {
                when (it.data!!.response) {
                    0 -> binding.edtPost.error = it.message
                    1 -> shortToast(it.message!!)
                    2 -> {
                        showProgress("Adding...", false)
                        if (isUploaded) {
                            addUvisViewModel.addUvis()
                        } else {
                            uploadFile(
                                Uri.fromFile(File(intent.getStringExtra("video_path").toString())),
                                addUvisViewModel.uvis.id.toString(),
                                "uvis/",
                                object : UploadFileResponse {
                                    override fun onSuccess(filePath: String) {
                                        isUploaded = true
                                        addUvisViewModel.uvis.url = filePath
                                        addUvisViewModel.addUvis()
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
                    }
                }
            }
        })

        addUvisViewModel.message.observe(this, { event ->
            dismissProgress()
            event.getContentIfNotHandled()?.let {
                when (it.data!!.response) {
                    0 -> shortToast(it.message!!)
                    1 -> {
                        shortToast(it.message!!)
                        if (binding.promote.isChecked) {
                            startActivity(
                                Intent(this, PromotePostActivity::class.java)
                                    .putExtra("bundle", Bundle().apply {
                                        putParcelable("uvis", addUvisViewModel.uvis)
                                        putInt("type", 1)
                                    })
                                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            )
                            finish()
                        } else {
                            startActivity(
                                Intent(this, MainActivity::class.java)
                                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            )
                            finish()
                        }
                    }
                }
            }
        })

        binding.txtPost.setOnClickListener {
            addUvisViewModel.uvis.id = System.currentTimeMillis().toString()
            addUvisViewModel.uvis.username = getUsername()
            addUvisViewModel.uvis.profileUrl = getProfileUrl()
            addUvisViewModel.uvis.profession = getProfession()
            addUvisViewModel.uvis.hashTags =
                binding.edtHashTags.text.toString().replace(" ", "").split("#").toString()
            addUvisViewModel.uvis.description = binding.edtPost.text.toString()

            addUvisViewModel.validate()
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        AppUtil.releaseUtils()
    }

}