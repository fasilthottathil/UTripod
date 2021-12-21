package com.aitechnologies.utripod.ui.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.aitechnologies.utripod.databinding.ActivityMyProfileBinding
import com.aitechnologies.utripod.ui.fragments.MyProfileFragment
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
class MyProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMyProfileBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportFragmentManager.beginTransaction()
            .replace(binding.layout.id, MyProfileFragment())
            .commit()


    }
}