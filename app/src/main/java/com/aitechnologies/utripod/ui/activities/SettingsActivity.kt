package com.aitechnologies.utripod.ui.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.aitechnologies.utripod.R
import com.aitechnologies.utripod.databinding.ActivitySettingsBinding
import com.aitechnologies.utripod.interfaces.UploadFileResponse
import com.aitechnologies.utripod.models.Users
import com.aitechnologies.utripod.repository.UserRepository
import com.aitechnologies.utripod.ui.viewModels.SettingsViewModel
import com.aitechnologies.utripod.ui.viewModels.SettingsViewModelProvider
import com.aitechnologies.utripod.util.AppSharedPreference.Companion.updateProfileUrl
import com.aitechnologies.utripod.util.AppUtil.Companion.dismissProgress
import com.aitechnologies.utripod.util.AppUtil.Companion.shortToast
import com.aitechnologies.utripod.util.AppUtil.Companion.showProgress
import com.aitechnologies.utripod.util.AppUtil.Companion.uploadFile
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

class SettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingsBinding
    private lateinit var settingsViewModel: SettingsViewModel
    private var professionList = emptyArray<String>()
    private var actualPassword = ""
    private var users = Users()
    private var uri: Uri? = null
    private lateinit var imageResultLauncher: ActivityResultLauncher<Intent>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val userRepository = UserRepository()
        val settingsViewModelProvider = SettingsViewModelProvider(userRepository, application)

        settingsViewModel = ViewModelProvider(
            this,
            settingsViewModelProvider
        )[SettingsViewModel::class.java]


        imageResultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            if (it.resultCode == RESULT_OK) {
                uri = it.data!!.data
                Glide.with(this)
                    .load(uri)
                    .apply(RequestOptions.circleCropTransform())
                    .into(binding.imageview)
            }
        }

        users = intent.getSerializableExtra("users") as Users

        actualPassword = users.password


        setupSpinner()

        Glide.with(this)
            .load(users.profileUrl)
            .apply(RequestOptions.circleCropTransform())
            .into(binding.imageview)

        binding.firstname.setText(users.name)
        binding.edtNumber.setText(users.phone)
        binding.bio.setText(users.bio)
        var position = 0
        for (i in professionList.indices) {
            if (professionList[i] == users.profession) {
                position = i
                break
            }
        }

        binding.professionSpinner.setSelection(position)

        settingsViewModel.isUpdated.observe(this, {
            dismissProgress()
            if (it) {
                shortToast("Updated")
                if (uri != null)
                    updateProfileUrl(users.profileUrl)
                startActivity(
                    Intent(this, MainActivity::class.java)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                )
                finish()
            } else {
                shortToast("An error occurred")
            }
        })

        binding.savechanges.setOnClickListener {
            users.name = binding.firstname.text.toString()
            users.bio = binding.bio.text.toString()
            users.phone = binding.edtNumber.text.toString()
            users.password = binding.password.text.toString()

            settingsViewModel.validate(users)

        }

        settingsViewModel.validate.observe(this, { event ->
            event.getContentIfNotHandled()?.let {
                when (it.data!!.response) {
                    0 -> binding.firstname.error = it.message
                    1 -> binding.edtNumber.error = it.message
                    2 -> binding.password.error = it.message
                    3 -> shortToast(it.message.toString())
                    4 -> {
                        if (actualPassword == users.password) {
                            showProgress("Updating..", false)
                            if (uri == null) {
                                settingsViewModel.updateProfile(users)
                            } else {
                                uploadFile(
                                    uri!!,
                                    users.username,
                                    "profile/",
                                    object : UploadFileResponse {
                                        override fun onSuccess(filePath: String) {
                                            users.profileUrl = filePath
                                            settingsViewModel.updateProfile(users)
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
                        } else {
                            binding.password.error = "Incorrect password"
                        }
                    }
                }
            }
        })

        binding.imageview.setOnClickListener { selectImage() }
        binding.storyPlus2.setOnClickListener { selectImage() }

        binding.cp.setOnClickListener {
            startActivity(
                Intent(this, ChangePasswordActivity::class.java)
                    .putExtra("password", users.password)
            )
        }

    }

    private fun selectImage() {
        imageResultLauncher.launch(Intent().apply {
            action = Intent.ACTION_GET_CONTENT
            type = "image/*"
        })
    }

    private fun setupSpinner() {
        professionList = resources.getStringArray(R.array.profession)


        binding.professionSpinner.apply {
            adapter = ArrayAdapter(
                this@SettingsActivity,
                R.layout.drop_down_text_white_item, professionList
            )
            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    users.profession = professionList[p2]
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {

                }
            }
        }
    }
}