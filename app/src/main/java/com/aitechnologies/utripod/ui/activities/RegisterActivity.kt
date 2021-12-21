package com.aitechnologies.utripod.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.aitechnologies.utripod.R
import com.aitechnologies.utripod.databinding.ActivityRegisterBinding
import com.aitechnologies.utripod.repository.RegisterRepository
import com.aitechnologies.utripod.repository.RegisterRepository.Companion.ALREADY_EXIST
import com.aitechnologies.utripod.repository.RegisterRepository.Companion.AUTH_ERROR
import com.aitechnologies.utripod.repository.RegisterRepository.Companion.REGISTER_SUCCESS
import com.aitechnologies.utripod.ui.viewModels.RegisterProvider
import com.aitechnologies.utripod.ui.viewModels.RegisterViewModel
import com.aitechnologies.utripod.ui.viewModels.RegisterViewModel.Companion.INVALID_LOCATION
import com.aitechnologies.utripod.ui.viewModels.RegisterViewModel.Companion.INVALID_NAME
import com.aitechnologies.utripod.ui.viewModels.RegisterViewModel.Companion.INVALID_PASSWORD
import com.aitechnologies.utripod.ui.viewModels.RegisterViewModel.Companion.INVALID_PASSWORD_C
import com.aitechnologies.utripod.ui.viewModels.RegisterViewModel.Companion.INVALID_PHONE
import com.aitechnologies.utripod.ui.viewModels.RegisterViewModel.Companion.INVALID_USERNAME
import com.aitechnologies.utripod.ui.viewModels.RegisterViewModel.Companion.NO_CONNECTION
import com.aitechnologies.utripod.ui.viewModels.RegisterViewModel.Companion.VALID
import com.aitechnologies.utripod.util.AppSharedPreference.Companion.login
import com.aitechnologies.utripod.util.AppUtil.Companion.shortToast
import com.aitechnologies.utripod.util.NotificationUtil
import java.util.*

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private val genderList = arrayListOf("Male", "Female", "Other")
    private var professionList = emptyArray<String>()
    private var ageList = emptyArray<String>()
    private lateinit var registerViewModel: RegisterViewModel
    private lateinit var phoneAuthResultLauncher: ActivityResultLauncher<Intent>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.statusBarColor = getColor(R.color.colorAccent)

        val provider = RegisterProvider(RegisterRepository(), application)

        registerViewModel = ViewModelProvider(this, provider)[RegisterViewModel::class.java]

        setupSpinner()


        val locationSelectLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            if (it.resultCode == RESULT_OK) {
                val extras = it.data!!.extras!!
                binding.txtLocation.text = extras.getString("location").toString()
                registerViewModel.users.location = extras.getString("location").toString()
                registerViewModel.users.region = extras.getString("region").toString()
            }
        }

        phoneAuthResultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            if (it.resultCode == RESULT_OK) {
                showLoading()
                registerViewModel.register()
            }
        }

        binding.txtLocation.setOnClickListener {
            locationSelectLauncher.launch(
                Intent(this, SearchLocationActivity::class.java)
            )
        }

        binding.btnRegister.setOnClickListener {
            registerViewModel.users.name = binding.edtName.text.toString()
            registerViewModel.users.username = binding.edtUsername.text.toString()
            registerViewModel.users.password = binding.edtPassword.text.toString()
            registerViewModel.password = binding.edtPasswordConfirm.text.toString()
            registerViewModel.users.phone = binding.edtNumber.text.toString()

            registerViewModel.validate()
        }

        registerViewModel.validate.observe(this, {
            it.getContentIfNotHandled()?.let { resource ->
                when (resource.data!!.response) {
                    INVALID_NAME -> binding.edtName.error = resource.message
                    INVALID_USERNAME -> binding.edtUsername.error = resource.message
                    INVALID_PASSWORD -> binding.edtPassword.error = resource.message
                    INVALID_PASSWORD_C -> binding.edtPasswordConfirm.error = resource.message
                    INVALID_PHONE -> binding.edtNumber.error = resource.message
                    INVALID_LOCATION -> shortToast(resource.message.toString())
                    NO_CONNECTION -> shortToast(resource.message.toString())
                    VALID -> {
                        phoneAuthResultLauncher.launch(
                            Intent(this, PhoneAuthenticationActivity::class.java)
                                .putExtra("phone", registerViewModel.users.phone)
                        )
                    }
                }
            }
        })

        registerViewModel.message.observe(this, {
            it.getContentIfNotHandled()?.let { response ->
                hideLoading()
                when (response.response) {
                    AUTH_ERROR -> shortToast("Authentication error!")
                    ALREADY_EXIST -> binding.edtUsername.error = "Username is taken"
                    REGISTER_SUCCESS -> {
                        login(
                            registerViewModel.users.username,
                            registerViewModel.users.userId,
                            registerViewModel.users.profileUrl,
                            registerViewModel.users.profession
                        )
                        NotificationUtil.subscribeToTopics(application)
                        startActivity(
                            Intent(this, SetBioAndImageActivity::class.java)
                        )
                        finish()
                    }
                }
            }
        })

    }

    private fun hideLoading() {
        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        binding.progressCircular.visibility = INVISIBLE
    }

    private fun showLoading() {
        window.addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        binding.progressCircular.visibility = VISIBLE
    }

    private fun setupSpinner() {
        binding.genderSpinner.apply {
            adapter = ArrayAdapter(
                this@RegisterActivity,
                R.layout.support_simple_spinner_dropdown_item, genderList
            )
            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    registerViewModel.users.gender = genderList[p2]
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {

                }
            }
        }

        ageList = resources.getStringArray(R.array.age)

        binding.ageSpinner.apply {
            adapter = ArrayAdapter(
                this@RegisterActivity,
                R.layout.support_simple_spinner_dropdown_item, ageList
            )
            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    if (p2 != 0){
                        registerViewModel.users.age = ageList[p2]
                    }
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {

                }
            }
        }

        professionList = resources.getStringArray(R.array.profession)

        registerViewModel.users.gender = genderList[0]
        registerViewModel.users.profession = professionList[0]
        registerViewModel.users.userId = UUID.randomUUID().toString().replace("-", "")

        binding.professionSpinner.apply {
            adapter = ArrayAdapter(
                this@RegisterActivity,
                R.layout.support_simple_spinner_dropdown_item, professionList
            )
            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    registerViewModel.users.profession = professionList[p2]
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {

                }
            }
        }

    }
}