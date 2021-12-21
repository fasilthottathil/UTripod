package com.aitechnologies.utripod.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.aitechnologies.utripod.R
import com.aitechnologies.utripod.databinding.ActivityPromotePostBinding
import com.aitechnologies.utripod.models.PostPromotion
import com.aitechnologies.utripod.models.Posts
import com.aitechnologies.utripod.models.Uvis
import com.aitechnologies.utripod.models.UvisPromotion
import com.aitechnologies.utripod.repository.PostRepository
import com.aitechnologies.utripod.repository.UserRepository
import com.aitechnologies.utripod.repository.UvisRepository
import com.aitechnologies.utripod.ui.viewModels.PromotePostViewModel
import com.aitechnologies.utripod.ui.viewModels.PromotePostViewModelProvider
import com.aitechnologies.utripod.util.AppSharedPreference.Companion.getUsername
import com.aitechnologies.utripod.util.AppUtil.Companion.isConnected
import com.aitechnologies.utripod.util.AppUtil.Companion.shortToast
import com.google.firebase.Timestamp
import com.razorpay.Checkout
import com.razorpay.PaymentResultListener
import org.json.JSONObject
import timber.log.Timber
import java.util.*


class PromotePostActivity : AppCompatActivity(), PaymentResultListener {
    private lateinit var binding: ActivityPromotePostBinding
    private lateinit var promotePostViewModel: PromotePostViewModel
    private var locationList: ArrayList<String> = arrayListOf()
    private var stateList: ArrayList<String> = arrayListOf()
    private var price = 50
    private var actualPrice = price
    private var posts = Posts()
    private var uvis = Uvis()
    private var type = 0
    private var region = ""
    private var location = ""
    private var postPromotion = PostPromotion()
    private var uvisPromotion = UvisPromotion()
    private val daysList = arrayListOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 15, 20, 30)
    private var days = 1
    private var isFromHome = false
    private lateinit var checkout: Checkout
    private var isFirst = true
    private var isAllUsersLoaded = false
    private var isAllOverIndia = true
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPromotePostBinding.inflate(layoutInflater)
        setContentView(binding.root)


        setupCheckBox()

        setupRazorPay()


        val bundle = intent.getBundleExtra("bundle")!!
        type = bundle.getInt("type")
        if (type == 0)
            posts = bundle.getParcelable<Posts>("post") as Posts
        else
            uvis = bundle.getParcelable<Uvis>("uvis") as Uvis

        isFromHome = bundle.getBoolean("isFromHome")

        if (type == 0) {
            postPromotion.id = posts.id!!
            postPromotion.username = posts.username!!
            postPromotion.profileUrl = posts.profileUrl!!
            postPromotion.post = posts.post!!
            postPromotion.type = posts.type!!
            postPromotion.likes = posts.likes!!
            postPromotion.comments = posts.comments!!
            postPromotion.shares = posts.shares!!
            postPromotion.description = posts.description!!
            postPromotion.hashTags = posts.hashTags!!
            postPromotion.likesList = posts.likesList!!
            postPromotion.isPublic = posts.isPublic!!
            postPromotion.profession = posts.profession!!
        } else {
            uvisPromotion.id = uvis.id!!
            uvisPromotion.profileUrl = uvis.profileUrl!!
            uvisPromotion.url = uvis.url!!
            uvisPromotion.likes = uvis.likes!!
            uvisPromotion.comments = uvis.comments!!
            uvisPromotion.shares = uvis.shares!!
            uvisPromotion.description = uvis.description!!
            uvisPromotion.hashTags = uvis.hashTags!!
            uvisPromotion.likesList = uvis.likesList!!
            uvisPromotion.isPublic = uvis.isPublic!!
            uvisPromotion.profession = uvis.profession!!
        }

        val promotePostViewModelProvider = PromotePostViewModelProvider(
            UserRepository(),
            PostRepository(application),
            UvisRepository(application)
        )

        promotePostViewModel = ViewModelProvider(
            this,
            promotePostViewModelProvider
        )[PromotePostViewModel::class.java]

        promotePostViewModel.getAllUsers()



        promotePostViewModel.isAdded.observe(this, {
            hideLoading()
            if (it) {
                shortToast("Promoted successfully")
                finish()
            } else {
                shortToast("An error occurred")
            }
        })

        promotePostViewModel.users.observe(this, {
            if (!isAllUsersLoaded) {
                it.forEach { user ->
                    if (!locationList.contains(user.location))
                        locationList.add(user.location)
                    if (!stateList.contains(user.region))
                        stateList.add(user.region)
                }
                isAllUsersLoaded = true
                setupSpinner()
            }
            hideLoading()
            actualPrice = if (it.isEmpty()) {
                "Estimated reach ${it.size} users".also { reach -> binding.txtReach.text = reach }
                10
            } else {
                "Estimated reach ${it.size} users".also { reach -> binding.txtReach.text = reach }
                it.size * 5
            }
            calculatePrice()
        })

        binding.promote.setOnClickListener {
            if (isConnected()) {
                showLoading()
                startPayment()
            }
        }

        binding.back.setOnClickListener { onBackPressed() }

    }

    private fun setupRazorPay() {
        checkout = Checkout()
        checkout.setImage(R.drawable.ic_logo)
        checkout.setKeyID("rzp_live_kvgchGZT61e3Fn")
    }

    private fun startPayment() {
        try {
            val options = JSONObject()
            options.put("name", "UTripod")
            options.put("description", System.currentTimeMillis().toString() + getUsername())
            options.put("theme.color", "#C41E2F")
            options.put("currency", "INR")
            options.put("amount", price * 100)
            val retryObj = JSONObject()
            retryObj.put("enabled", true)
            retryObj.put("max_count", 4)
            options.put("retry", retryObj)
            checkout.open(this, options)
        } catch (e: Exception) {
            hideLoading()
            shortToast("An error occurred")
        }
    }

    private fun setupCheckBox() {
        binding.allOverIndia.setBackgroundResource(R.drawable.selected_button)

        binding.allOverIndia.setOnClickListener {
            isAllOverIndia = true
            binding.allOverIndia.setBackgroundResource(R.drawable.selected_button)
            binding.selectedRegion.setBackgroundResource(R.drawable.promotion_button)
            binding.selectedLocation.setBackgroundResource(R.drawable.promotion_button)
            binding.cardViewstate.visibility = INVISIBLE
            binding.cardViewLocation.visibility = INVISIBLE
            showLoading()
            promotePostViewModel.getAllUsers()
        }

        binding.selectedLocation.setOnClickListener {
            isAllOverIndia = false
            binding.selectedLocation.setBackgroundResource(R.drawable.selected_button)
            binding.selectedRegion.setBackgroundResource(R.drawable.promotion_button)
            binding.allOverIndia.setBackgroundResource(R.drawable.promotion_button)
            binding.cardViewstate.visibility = INVISIBLE
            binding.cardViewLocation.visibility = VISIBLE
            showLoading()
            promotePostViewModel.getUsersByLocation(location)
        }

        binding.selectedRegion.setOnClickListener {
            isAllOverIndia = false
            binding.selectedRegion.setBackgroundResource(R.drawable.selected_button)
            binding.allOverIndia.setBackgroundResource(R.drawable.promotion_button)
            binding.selectedLocation.setBackgroundResource(R.drawable.promotion_button)
            binding.cardViewstate.visibility = VISIBLE
            binding.cardViewLocation.visibility = INVISIBLE
            showLoading()
            promotePostViewModel.getUsersByRegion(region)
        }
    }


    private fun setupSpinner() {

        region = if (stateList.isNotEmpty()) stateList[0]
        else
            ""

        location = if (locationList.isNotEmpty()) locationList[0]
        else
            ""

        binding.dropdownstate.apply {
            adapter = ArrayAdapter(
                this@PromotePostActivity,
                R.layout.drop_down_text_white_item,
                stateList
            )
            onItemSelectedListener = object : AdapterView.OnItemClickListener,
                AdapterView.OnItemSelectedListener {
                override fun onItemClick(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {

                }

                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    showLoading()
                    region = stateList[p2]
                    if (isFirst) {
                        isFirst = false
                        promotePostViewModel.getAllUsers()
                    } else
                        promotePostViewModel.getUsersByRegion(region)
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {

                }
            }
        }

        binding.dropDownLocation.apply {
            adapter = ArrayAdapter(
                this@PromotePostActivity,
                R.layout.drop_down_text_white_item,
                locationList
            )
            onItemSelectedListener = object : AdapterView.OnItemClickListener,
                AdapterView.OnItemSelectedListener {
                override fun onItemClick(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {

                }

                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    showLoading()
                    location = locationList[p2]
                    if (isFirst) {
                        isFirst = false
                        promotePostViewModel.getAllUsers()
                    } else
                        promotePostViewModel.getUsersByLocation(location)
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {

                }
            }
        }

        binding.dropDownDays.apply {
            adapter = ArrayAdapter(
                this@PromotePostActivity,
                R.layout.drop_down_text_white_item,
                daysList
            )
            onItemSelectedListener = object : AdapterView.OnItemClickListener,
                AdapterView.OnItemSelectedListener {
                override fun onItemClick(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {

                }

                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    days = daysList[p2]
                    calculatePrice()
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {

                }
            }
        }

    }

    private fun calculatePrice() {
        price = actualPrice
        for (i in 0 until days) {
            price += 10
        }
        "Price : $price inr".also { text -> binding.tvprice.text = text }
    }

    private fun showLoading() {
        window.addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        binding.progressCircular.visibility = VISIBLE
    }

    private fun hideLoading() {
        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        binding.cardViewday.visibility = VISIBLE
        binding.progressCircular.visibility = INVISIBLE
        binding.loadingLayout.visibility = INVISIBLE
    }

    override fun onPaymentSuccess(p0: String?) {
        if (type == 0) {
            val timestamp = days * 24 * 60 * 60 * 1000
            postPromotion.isState = binding.cardViewstate.visibility == VISIBLE
            if (postPromotion.isState!!)
                postPromotion.region = region
            else
                postPromotion.region = location
            if (isAllOverIndia)
                postPromotion.region = "All over india"
            postPromotion.toDate = Timestamp(Date(System.currentTimeMillis() + timestamp))
            promotePostViewModel.promotePost(postPromotion)
        } else {
            val timestamp = days * 24 * 60 * 60 * 1000
            uvisPromotion.isState = binding.cardViewstate.visibility == VISIBLE
            if (uvisPromotion.isState!!)
                uvisPromotion.region = region
            else
                uvisPromotion.region = location
            if (isAllOverIndia)
                uvisPromotion.region = "All over india"
            uvisPromotion.toDate = Timestamp(Date(System.currentTimeMillis() + timestamp))
            promotePostViewModel.promoteUvis(uvisPromotion)
        }
    }

    override fun onPaymentError(p0: Int, p1: String?) {
        hideLoading()
        Timber.tag("RazorPay Error:::::::::").e(p1.toString())
        shortToast("Payment cancelled or failed")
    }

    override fun onBackPressed() {
        if (type == 0 || isFromHome) {
            super.onBackPressed()
        } else {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}
