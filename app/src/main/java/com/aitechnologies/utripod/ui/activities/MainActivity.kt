package com.aitechnologies.utripod.ui.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.aitechnologies.utripod.adapters.HomeNavAdapter
import com.aitechnologies.utripod.camera.PortraitCameraActivity
import com.aitechnologies.utripod.databinding.ActivityMainBinding
import com.aitechnologies.utripod.services.OfflineService
import com.aitechnologies.utripod.ui.viewModels.MainProvider
import com.aitechnologies.utripod.ui.viewModels.MainViewModel
import com.aitechnologies.utripod.util.AppUtil.Companion.shortToast


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var permissionResultLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var mainViewModel: MainViewModel
    private lateinit var mainProvider: MainProvider
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViewPager()
        setupBottomNav()

        mainProvider = MainProvider(application)
        mainViewModel = ViewModelProvider(this, mainProvider)[MainViewModel::class.java]

        mainViewModel.setOnline()

        startService(Intent(this, OfflineService::class.java))

        permissionResultLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) {
            var isGranted = true
            for ((_, value) in it) {
                if (!value)
                    isGranted = false
            }
            if (isGranted) {
                startCamera()
            } else
                shortToast("Denied")
        }

        binding.txtPost.setOnClickListener { changePager(2) }
        binding.txtUvis.setOnClickListener {
            if (checkPermissions()) {
                startCamera()
            } else {
                permissionResultLauncher.launch(
                    arrayOf(
                        Manifest.permission.CAMERA,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.RECORD_AUDIO,
                    )
                )
            }
        }


        binding.txtCancel.setOnClickListener { binding.layoutAdd.visibility = INVISIBLE }
    }

    private fun startCamera() {
        binding.layoutAdd.visibility = INVISIBLE
        startActivity(Intent(this, PortraitCameraActivity::class.java))
    }

    private fun checkPermissions(): Boolean {
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            return false
        }
        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            return false
        }
        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            return false
        }
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            return false
        }
        return true
    }

    private fun setupBottomNav() {
        binding.imgHome.setOnClickListener { changePager(0) }
        binding.imgChats.setOnClickListener { changePager(1) }
        binding.orderPlus.setOnClickListener {
            if (binding.layoutAdd.visibility == VISIBLE)
                binding.layoutAdd.visibility = INVISIBLE
            else
                binding.layoutAdd.visibility = VISIBLE
        }
        binding.imgSearch.setOnClickListener { changePager(3) }
        binding.imgProfile.setOnClickListener { changePager(4) }
    }

    private fun changePager(position: Int) {
        binding.layoutAdd.visibility = INVISIBLE
        binding.viewpager.setCurrentItem(position, false)
    }


    private fun setupViewPager() {
        binding.viewpager.apply {
            isUserInputEnabled = false
            adapter = HomeNavAdapter(supportFragmentManager, lifecycle)
        }
    }

}