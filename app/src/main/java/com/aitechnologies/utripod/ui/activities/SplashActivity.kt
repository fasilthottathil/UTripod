package com.aitechnologies.utripod.ui.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.aitechnologies.utripod.R
import com.aitechnologies.utripod.ui.viewModels.SplashViewModel
import com.aitechnologies.utripod.ui.viewModels.SplashViewModelProvider

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    private lateinit var splashViewModel: SplashViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val providerFactory = SplashViewModelProvider(application)

        splashViewModel = ViewModelProvider(this, providerFactory)[SplashViewModel::class.java]

        splashViewModel.isLogin()

        splashViewModel.isLogin.observe(this, {
            if (it)
                startActivity(Intent(this, MainActivity::class.java))
            else
                startActivity(Intent(this, LoginActivity::class.java))
            finish()
        })

    }
}