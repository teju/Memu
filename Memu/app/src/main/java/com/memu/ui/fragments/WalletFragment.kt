package com.memu.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.iapps.gon.etc.callback.NotifyListener
import com.iapps.gon.etc.callback.WalletBalanceListener
import com.iapps.libs.helpers.BaseHelper
import com.memu.R
import com.memu.etc.UserInfoManager
import com.memu.modules.checksum.WalletBalance
import com.memu.ui.BaseFragment
import com.memu.webservices.GetCheckSumViewModel
import com.memu.webservices.GetWalletBalanceViewModel
import com.memu.webservices.PostMakePaymentViewModel
import com.paytm.pgsdk.Log
import com.paytm.pgsdk.PaytmOrder
import com.paytm.pgsdk.PaytmPGService
import com.paytm.pgsdk.PaytmPaymentTransactionCallback
import kotlinx.android.synthetic.main.fragment_wallet.*
import kotlinx.android.synthetic.main.profile_header.*
import java.util.*
import kotlin.collections.HashMap


class WalletFragment : BaseFragment(), PaytmPaymentTransactionCallback,View.OnClickListener,WalletBalanceListener {

    lateinit var getchecksumviewmodel: GetCheckSumViewModel
    lateinit var postMakePaymentViewModel: PostMakePaymentViewModel

    var TAG = "check-sum"
    var orderId="1001"
    var custid = "123"
    var CHECKSUMHASH = ""
    var wallet_Balance = ""
    var isFromHome = false
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        v = inflater.inflate(R.layout.fragment_wallet, container, false)
        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setGetCheckSUMRequestObserver()
        initUI();
    }

    private fun initUI() {
        setUSerMAinDataAPIObserver()
        setWalletBalanceObserver(this)
        setGetCheckSUMRequestObserver()
        setPaymentAPIObserver()
        ll_options.visibility = View.GONE
        recharge.setOnClickListener(this)
        arrow_left.setOnClickListener(this)
        posUserMainDataViewModel.loadData(UserInfoManager.getInstance(activity!!).getAccountId())
        getWalletBalanceViewModel.loadData()
        recharge_200.setOnClickListener(this)
        recharge_500.setOnClickListener(this)
        recharge_1000.setOnClickListener(this)
        refer_now.setOnClickListener(this)
        withdraw.setOnClickListener(this)
    }

    fun setPaymentAPIObserver() {
        postMakePaymentViewModel = ViewModelProviders.of(this).get(
            PostMakePaymentViewModel::class.java).apply {
            this@WalletFragment.let { thisFragReference ->
                isLoading.observe(thisFragReference, Observer { aBoolean ->
                    if(aBoolean!!) {
                        ld.showLoadingV2()
                    } else {
                        ld.hide()
                    }
                })
                errorMessage.observe(thisFragReference, Observer { s ->
                    showNotifyDialog(
                        s.title, s.message!!,
                        getString(R.string.ok),"",object : NotifyListener {
                            override fun onButtonClicked(which: Int) { }
                        }
                    )
                })
                isNetworkAvailable.observe(thisFragReference, obsNoInternet)
                getTrigger().observe(thisFragReference, Observer { state ->
                    when (state) {
                        GetWalletBalanceViewModel.NEXT_STEP -> {
                            getWalletBalanceViewModel.loadData()
                        }
                    }
                })
            }
        }
    }

    fun setGetCheckSUMRequestObserver() {
        getchecksumviewmodel = ViewModelProviders.of(this).get(
            GetCheckSumViewModel::class.java).apply {
            this@WalletFragment.let { thisFragReference ->
                isLoading.observe(thisFragReference, Observer { aBoolean ->
                    if(aBoolean!!) {
                        ld.showLoadingV2()
                    } else {
                        ld.hide()
                    }
                })
                errorMessage.observe(thisFragReference, Observer { s ->
                    showNotifyDialog(
                        s.title, s.message!!,
                        getString(R.string.ok),"",object : NotifyListener {
                            override fun onButtonClicked(which: Int) { }
                        }
                    )
                })
                isNetworkAvailable.observe(thisFragReference, obsNoInternet)
                getTrigger().observe(thisFragReference, Observer { state ->
                    when (state) {
                        GetCheckSumViewModel.NEXT_STEP -> {
                            if(getchecksumviewmodel.obj?.generate_signature != null) {
                                CHECKSUMHASH = getchecksumviewmodel.obj?.generate_signature!!
                            }
                            val amt = amount.text.toString().toDouble()
                            val Service = PaytmPGService.getProductionService()
                            val paramMap =
                                HashMap<String, String>()
                            //these are mandatory parameters
                            paramMap["MID"] = getString(R.string.mid) //MID provided by paytm
                            paramMap["CUST_ID"] = UserInfoManager.getInstance(apl!!).getAccountId()
                            paramMap["ORDER_ID"] = orderId
                            paramMap["CHANNEL_ID"] = "WAP"
                            paramMap["TXN_AMOUNT"] = amt.toString()
                            paramMap["WEBSITE"] = "DEFAULT"
                            paramMap["CALLBACK_URL"] = "https://securegw.paytm.in/theia/paytmCallback?ORDER_ID=" + orderId
                           // paramMap.put("EMAIL", "daya_salagare@yahoo.com");   // no need
                          // paramMap.put("MOBILE_NO", "9964062237");  // no need
                           paramMap["CHECKSUMHASH"] = CHECKSUMHASH
                           // paramMap.put("PAYMENT_TYPE_ID", "CC");    // no need
                            paramMap["INDUSTRY_TYPE_ID"] = "Retail"
                            val Order = PaytmOrder(paramMap)
                            Log.d(TAG,"checksum "+"param $paramMap")
                            System.out.println("check-sum "+"param $paramMap")

                            Log.e("checksum ", "param $paramMap")
                            Service.initialize(Order, null)
                            // start payment service call here
                            Service.startPaymentTransaction(
                                context, true, true,
                                this@WalletFragment)

                        }
                    }
                })
            }
        }
    }

    override fun onTransactionResponse(inResponse: Bundle?) {
        Log.d(TAG,"inResponse "+inResponse!!.getString("RESPCODE"))
        if(inResponse!!.getString("RESPCODE") == "01"){
            postMakePaymentViewModel.loadData("wallet",amount.text.toString(),wallet_Balance,"","","","","","")
        } else {
            showNotifyDialog(
                "", inResponse.getString("RESPMSG"),
                getString(R.string.ok),"",object : NotifyListener {
                    override fun onButtonClicked(which: Int) { }
                }
            )
        }
    }

    override fun clientAuthenticationFailed(inErrorMessage: String?) {
        Log.d(TAG,"inErrorMessage "+inErrorMessage.toString())
        showNotifyDialog(
            "", inErrorMessage.toString(),
            getString(R.string.ok),"",object : NotifyListener {
                override fun onButtonClicked(which: Int) { }
            }
        )

    }

    override fun someUIErrorOccurred(inErrorMessage: String?) {
        Log.d(TAG,"someUIErrorOccurred "+inErrorMessage.toString())
        showNotifyDialog(
            "", inErrorMessage.toString(),
            getString(R.string.ok),"",object : NotifyListener {
                override fun onButtonClicked(which: Int) { }
            }
        )
    }

    override fun onTransactionCancel(inErrorMessage: String?, inResponse: Bundle?) {
        Log.d(TAG,"onTransactionCancel "+inErrorMessage.toString())
        showNotifyDialog(
            "", inErrorMessage.toString(),
            getString(R.string.ok),"",object : NotifyListener {
                override fun onButtonClicked(which: Int) { }
            }
        )
    }

    override fun networkNotAvailable() {
        Log.d(TAG,"networkNotAvailable ")
    }

    override fun onErrorLoadingWebPage(
        iniErrorCode: Int,
        inErrorMessage: String?,
        inFailingUrl: String?
    ) {
        Log.d(TAG,"onErrorLoadingWebPage "+inErrorMessage)
        showNotifyDialog(
            "", inErrorMessage.toString(),
            getString(R.string.ok),"",object : NotifyListener {
                override fun onButtonClicked(which: Int) { }
            }
        )
    }

    override fun onBackPressedCancelTransaction() {
        Log.d(TAG,"onBackPressedCancelTransaction ")
        showNotifyDialog(
            "", "Transaction Cancelled",
            getString(R.string.ok),"",object : NotifyListener {
                override fun onButtonClicked(which: Int) { }
            }
        )
    }

    override fun onClick(v: View?) {
        when(v?.id) {
            R.id.recharge -> {
                llwithdraw.visibility = View.GONE

                orderId = "ID"+Random().nextInt()
                getchecksumviewmodel.loadData(custid,orderId,amount.text.toString().toDouble())
            }
            R.id.arrow_left -> {
                home().proceedDoOnBackPressed()
            }
            R.id.recharge_200 -> {
                var amt =  amount.text.toString().toInt()
                amt = amt + 200
                amount.setText(amt.toString())

            }
            R.id.recharge_500 -> {
                var amt =  amount.text.toString().toInt()
                amt = amt + 500
                amount.setText(amt.toString())
            }
            R.id.recharge_1000 -> {
                var amt =  amount.text.toString().toInt()
                amt = amt + 1000
                amount.setText(amt.toString())
            }
            R.id.refer_now -> {
                referFriend()
            }
            R.id.withdraw -> {
                scrollView.post {
                    scrollView.fullScroll(View.FOCUS_DOWN)
                }
                llwithdraw.visibility = View.VISIBLE
            }
        }
    }

    override fun walletBalanceResponse(balance: WalletBalance) {
        wallet_Balance = balance.balance!!
        walletBalance.setText(wallet_Balance)
    }
}
