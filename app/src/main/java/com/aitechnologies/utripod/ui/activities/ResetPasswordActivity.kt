package com.aitechnologies.utripod.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.WindowManager
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.aitechnologies.utripod.databinding.ActivityResetPasswordBinding
import com.aitechnologies.utripod.repository.UserRepository
import com.aitechnologies.utripod.ui.viewModels.ResetPasswordViewModel
import com.aitechnologies.utripod.ui.viewModels.ResetPasswordViewModelProvider
import com.aitechnologies.utripod.util.AppUtil.Companion.shortToast

class ResetPasswordActivity : AppCompatActivity() {
    private lateinit var binding: ActivityResetPasswordBinding
    private lateinit var resetPasswordViewModel: ResetPasswordViewModel
    private lateinit var phoneAuthenticationResultLauncher: ActivityResultLauncher<Intent>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResetPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val resetPasswordViewModelProvider = ResetPasswordViewModelProvider(
            UserRepository(),
            application
        )

        phoneAuthenticationResultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            if (it.resultCode == RESULT_OK) {
                resetPasswordViewModel.resetPassword()
            }
        }

        resetPasswordViewModel = ViewModelProvider(
            this,
            resetPasswordViewModelProvider
        )[ResetPasswordViewModel::class.java]

        resetPasswordViewModel.validate.observe(this, { event ->
            event.getContentIfNotHandled()?.let {
                when (it.data!!.response) {
                    0 -> binding.edtNumber.error = it.message
                    1 -> binding.edtPassword.error = it.message
                    2 -> binding.edtPasswordConfirm.error = it.message
                    3 -> shortToast(it.message.toString())
                    4 -> {
                        showLoading()
                        resetPasswordViewModel.getUserByPhone()
                    }
                }
            }
        })

        resetPasswordViewModel.message.observe(this, { event ->
            event.getContentIfNotHandled()?.let {
                when (it.data!!.response) {
                    0 -> {
                        hideLoading()
                        shortToast(it.message.toString())
                    }
                    1 -> {
                        phoneAuthenticationResultLauncher.launch(
                            Intent(this, PhoneAuthenticationActivity::class.java)
                                .putExtra("phone", resetPasswordViewModel.phone)
                        )
                    }
                    2 -> {
                        hideLoading()
                        shortToast(it.message.toString())
                        startActivity(
                            Intent(this, LoginActivity::class.java)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        )
                        finish()
                    }
                }
            }
        })

        binding.txtReset.setOnClickListener {
            resetPasswordViewModel.phone = binding.edtNumber.text.toString()
            resetPasswordViewModel.password = binding.edtPassword.text.toString()
            resetPasswordViewModel.passwordConfirm = binding.edtPasswordConfirm.text.toString()

            resetPasswordViewModel.validate()

        }

    }

    private fun hideLoading() {
        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        binding.progressCircular.visibility = INVISIBLE
    }

    private fun showLoading() {
        window.addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        binding.progressCircular.visibility = VISIBLE
    }
}