package com.aitechnologies.utripod.ui.activities

import android.os.Bundle
import android.os.CountDownTimer
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.aitechnologies.utripod.databinding.ActivityPhoneAuthenticationBinding
import com.aitechnologies.utripod.util.AppUtil.Companion.shortToast
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import timber.log.Timber
import java.util.concurrent.TimeUnit

class PhoneAuthenticationActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPhoneAuthenticationBinding
    private var phone = ""
    private var verificationId = ""
    private lateinit var countDownTimer: CountDownTimer
    private lateinit var options: PhoneAuthOptions
    private var counter = 30
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPhoneAuthenticationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        phone = intent.getStringExtra("phone").toString()

        phone = if (phone.length == 10){
            "+91$phone"
        }else{
            "+$phone"
        }

        "An OTP is send to $phone".also { binding.txtHead.text = it }

        setupAuthentication()

        startCounter()

        binding.txtVerify.setOnClickListener {
            if (binding.edtOtp.text.isEmpty() || binding.edtOtp.text.isBlank()) {
                binding.edtOtp.error = "Invalid otp"
                return@setOnClickListener
            }
            showLoading()
            verifyOtp(binding.edtOtp.text.toString())
        }

        binding.txtResend.setOnClickListener {
            binding.txtResend.visibility = INVISIBLE
            binding.txtCount.visibility = VISIBLE
            counter = 30
            startCounter()
            PhoneAuthProvider.verifyPhoneNumber(options)
        }

    }

    private fun startCounter() {
        countDownTimer = object : CountDownTimer(30000, 1000) {
            override fun onTick(p0: Long) {
                --counter
                "Resends in ${counter}s".also { binding.txtCount.text = it }
            }

            override fun onFinish() {
                binding.txtCount.visibility = INVISIBLE
                binding.txtResend.visibility = VISIBLE
            }
        }.start()
    }

    private fun showLoading() {
        window.addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        binding.progressCircular.visibility = VISIBLE
    }

    private fun verifyOtp(otp: String) {
        FirebaseAuth.getInstance()
            .signInWithCredential(PhoneAuthProvider.getCredential(verificationId, otp))
            .addOnSuccessListener {
                hideLoading()
                shortToast("Verification success")
                setResult(RESULT_OK)
                finish()
            }
            .addOnFailureListener {
                hideLoading()
                shortToast(it.message.toString())
            }
    }

    private fun hideLoading() {
        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        binding.progressCircular.visibility = INVISIBLE
    }

    private fun setupAuthentication() {
        options = PhoneAuthOptions.newBuilder()
            .setPhoneNumber(phone)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onCodeAutoRetrievalTimeOut(p0: String) {

                }

                override fun onCodeSent(p0: String, p1: PhoneAuthProvider.ForceResendingToken) {
                    verificationId = p0
                    super.onCodeSent(p0, p1)
                }

                override fun onVerificationCompleted(p0: PhoneAuthCredential) {
                    binding.edtOtp.setText(p0.smsCode)
                    shortToast("Verification success")
                    setResult(RESULT_OK)
                    finish()
                }

                override fun onVerificationFailed(p0: FirebaseException) {
                    shortToast(p0.message.toString())
                    Timber.d("PHONE_AUTHENTICATION ${p0.message.toString()}")
                }
            })
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    override fun onDestroy() {
        countDownTimer.cancel()
        super.onDestroy()
    }

}