package com.aitechnologies.utripod.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.aitechnologies.utripod.R
import com.aitechnologies.utripod.databinding.ActivityLoginBinding
import com.aitechnologies.utripod.models.Response
import com.aitechnologies.utripod.repository.LoginRepository
import com.aitechnologies.utripod.ui.viewModels.LoginViewModel
import com.aitechnologies.utripod.ui.viewModels.LoginViewModelProvider
import com.aitechnologies.utripod.util.AppUtil.Companion.shortToast
import com.aitechnologies.utripod.util.NotificationUtil

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var loginViewModel: LoginViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.statusBarColor = ContextCompat.getColor(this, R.color.colorAccent)

        val loginRepository = LoginRepository()
        val loginFactory = LoginViewModelProvider(loginRepository, application)

        loginViewModel = ViewModelProvider(this, loginFactory)[LoginViewModel::class.java]

        binding.btnLogin.setOnClickListener {
            loginViewModel.username = binding.edtUsername.text.toString()
            loginViewModel.password = binding.edtPassword.text.toString()
            loginViewModel.validate()
        }

        loginViewModel.validate.observe(this, { event ->
            event.getContentIfNotHandled()?.let {
                when ((it.data as Response).response) {
                    0 -> binding.edtUsername.error = it.message.toString()
                    1 -> binding.edtPassword.error = it.message.toString()
                    2 -> shortToast(it.message.toString())
                    3 -> {
                        showLoading()
                        loginViewModel.login()
                    }
                }
            }
        })

        loginViewModel.message.observe(this, { event ->
            event.getContentIfNotHandled()?.let {
                hideLoading()
                when ((it.data as Response).response) {
                    0 -> shortToast(it.message.toString())
                    1 -> binding.edtUsername.error = it.message.toString()
                    2 -> binding.edtPassword.error = it.message.toString()
                    3 -> {
                        NotificationUtil.subscribeToTopics(application)
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    }
                }
            }
        })

        binding.txtSignUp.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        binding.txtReset.setOnClickListener {
            startActivity(Intent(this, ResetPasswordActivity::class.java))
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