package com.aitechnologies.utripod.uvis.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.aitechnologies.utripod.databinding.ActivityEditUvisBinding
import com.aitechnologies.utripod.models.Uvis
import com.aitechnologies.utripod.repository.UvisRepository
import com.aitechnologies.utripod.ui.activities.MainActivity
import com.aitechnologies.utripod.util.AppUtil.Companion.shortToast
import com.aitechnologies.utripod.uvis.viewModels.EditUvisViewModel
import com.aitechnologies.utripod.uvis.viewModels.EditUvisViewModelProvider

class EditUvisActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditUvisBinding
    private var uvis = Uvis()
    private lateinit var editUvisViewModel: EditUvisViewModel
    private lateinit var editUvisViewModelProvider: EditUvisViewModelProvider
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditUvisBinding.inflate(layoutInflater)
        setContentView(binding.root)

        uvis = intent.getBundleExtra("bundle")!!.getParcelable<Uvis>("uvis") as Uvis

        editUvisViewModelProvider = EditUvisViewModelProvider(
            UvisRepository(application),
            application
        )

        editUvisViewModel = ViewModelProvider(
            this,
            editUvisViewModelProvider
        )[EditUvisViewModel::class.java]

        setUI()

        editUvisViewModel.validate.observe(this, { event ->
            event.getContentIfNotHandled()?.let {
                when (it.data!!.response) {
                    0 -> binding.edtPost.error = it.message
                    1 -> shortToast(it.message.toString())
                    2 -> {
                        showLoading()
                        editUvisViewModel.updateUvis()
                    }
                }
            }
        })

        editUvisViewModel.isUpdated.observe(this, {
            hideLoading()
            shortToast("Updated")
            startActivity(
                Intent(this, MainActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
            finish()
        })

        binding.update.setOnClickListener {
            uvis.description = binding.edtPost.text.toString()
            uvis.hashTags = binding.edtHashTags.text.toString()
            editUvisViewModel.uvis = uvis
            editUvisViewModel.validate()
        }

    }

    private fun hideLoading() {
        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        binding.progressCircular.visibility = View.INVISIBLE
    }

    private fun showLoading() {
        window.addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        binding.progressCircular.visibility = View.VISIBLE
    }

    private fun setUI() {
        var hashTags = ""
        uvis.hashTags!!.replace("[", "")
            .replace("]", "")
            .split(",")
            .forEach {
                if (it.isNotEmpty() && it.isNotBlank())
                    hashTags += "#$it"
            }
        binding.edtHashTags.setText(hashTags)
        binding.edtPost.setText(uvis.description)

        editUvisViewModel.uvis = uvis

    }
}