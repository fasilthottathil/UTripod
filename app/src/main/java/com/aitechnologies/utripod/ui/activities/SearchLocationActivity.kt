package com.aitechnologies.utripod.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.aitechnologies.utripod.adapters.LocationAdapter
import com.aitechnologies.utripod.databinding.ActivitySearchLocationBinding
import com.aitechnologies.utripod.ui.viewModels.LocationSearchProvider
import com.aitechnologies.utripod.ui.viewModels.LocationSearchViewModel
import com.aitechnologies.utripod.util.AppUtil.Companion.isConnected
import com.aitechnologies.utripod.util.AppUtil.Companion.shortToast
import com.aitechnologies.utripod.util.RetrofitInstance
import java.util.*

class SearchLocationActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySearchLocationBinding
    private lateinit var locationSearchViewModel: LocationSearchViewModel
    private val myAdapter by lazy { LocationAdapter() }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchLocationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val locationResponse = RetrofitInstance.create()

        val provider = LocationSearchProvider(locationResponse)
        locationSearchViewModel = ViewModelProvider(
            this,
            provider
        )[LocationSearchViewModel::class.java]

        myAdapter.setOnItemClickListener {
            val intent = Intent().apply {
                putExtra("location", it.data[0].name)
                putExtra("region", it.data[0].region)
            }
            setResult(RESULT_OK, intent)
            finish()
        }

        setupRecyclerView()

        locationSearchViewModel.location.observe(this, {
            hideLoading()
            if (it.data.isNotEmpty())
                myAdapter.setData(listOf(it))
            else
                shortToast("No location found")
        })



        binding.edtSearch.setOnEditorActionListener { _, i, _ ->
            if (i == EditorInfo.IME_ACTION_SEARCH &&
                binding.edtSearch.text.isNotBlank() &&
                binding.edtSearch.text.isNotEmpty() &&
                binding.progressCircular.visibility == INVISIBLE
            ) {
                if (!isConnected()) {
                    shortToast("No connection")
                } else {
                    if (binding.edtSearch.text.length < 3) {
                        binding.edtSearch.error = "Must be 3 characters"
                    } else {
                        showLoading()
                        locationSearchViewModel.searchLocation(binding.edtSearch.text.toString())
                    }
                }

            }
            true
        }

        locationSearchViewModel.message.observe(this, {
            it.getContentIfNotHandled()?.let { message ->
                hideLoading()
                shortToast(message)
            }
        })

    }

    private fun setupRecyclerView() {
        binding.rvLocation.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(this@SearchLocationActivity)
            adapter = myAdapter
        }
    }

    private fun showLoading() {
        binding.progressCircular.visibility = VISIBLE
    }

    private fun hideLoading() {
        binding.progressCircular.visibility = INVISIBLE
    }
}