package com.aitechnologies.utripod.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.aitechnologies.utripod.databinding.ActivityChangePasswordBinding
import com.aitechnologies.utripod.repository.UserRepository
import com.aitechnologies.utripod.ui.viewModels.ChangePasswordViewModel
import com.aitechnologies.utripod.ui.viewModels.ChangePasswordViewModelProvider
import com.aitechnologies.utripod.util.AppSharedPreference.Companion.getUsername
import com.aitechnologies.utripod.util.AppUtil.Companion.shortToast

class ChangePasswordActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChangePasswordBinding
    private lateinit var changePasswordViewModel: ChangePasswordViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChangePasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val userRepository = UserRepository()
        val changePasswordViewModelProvider =
            ChangePasswordViewModelProvider(userRepository, application)

        val password = intent.getStringExtra("password").toString()

        changePasswordViewModel = ViewModelProvider(
            this,
            changePasswordViewModelProvider
        )[ChangePasswordViewModel::class.java]

        changePasswordViewModel.isUpdated.observe(this, {
            hideLoading()
            if (it) {
                shortToast("Updated")
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

        changePasswordViewModel.validate.observe(this, { event ->
            event.getContentIfNotHandled()?.let {
                when (it.data!!.response) {
                    0 -> binding.newpassword.error = it.message
                    1 -> binding.oldpassword.error = it.message
                    2 -> shortToast(it.message.toString())
                    3 -> {
                        showLoading()
                        changePasswordViewModel.updatePassword(
                            getUsername(),
                            binding.newpassword.text.toString()
                        )
                    }
                }
            }
        })

        binding.update.setOnClickListener {
            if (password == binding.oldpassword.text.toString()) {
                changePasswordViewModel.validate(
                    binding.newpassword.text.toString(),
                    binding.confirmpassword.text.toString(),
                )
            } else {
                binding.oldpassword.error = "Incorrect password"
            }
        }

    }

    private fun hideLoading() {
        binding.progressbar.visibility = INVISIBLE
        binding.update.visibility = VISIBLE
    }

    private fun showLoading() {
        binding.progressbar.visibility = VISIBLE
        binding.update.visibility = INVISIBLE
    }
}