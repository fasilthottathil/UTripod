package com.aitechnologies.utripod.ui.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.aitechnologies.utripod.adapters.MyPromotionsTabAdapter
import com.aitechnologies.utripod.databinding.ActivityMyPromotionsBinding
import com.google.android.material.tabs.TabLayoutMediator

class MyPromotionsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMyPromotionsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyPromotionsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupTab()


    }

    private fun setupTab() {
        binding.viewpager.apply {
            adapter = MyPromotionsTabAdapter(supportFragmentManager, lifecycle)
            TabLayoutMediator(binding.tabLayout, this) { tab, position ->
                when (position) {
                    0 -> tab.text = "POST"
                    1 -> tab.text = "UVIS"
                }
                binding.viewpager.currentItem = tab.position
            }.attach()

        }
    }
}