package com.memu.ui.fragments

import android.app.ProgressDialog
import android.content.Context
import android.os.AsyncTask
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.iapps.gon.etc.callback.NotifyListener
import com.memu.R
import com.memu.etc.JSONParser
import com.memu.etc.UserInfoManager
import com.memu.ui.BaseFragment
import com.memu.webservices.GetCheckSumViewModel
import com.memu.webservices.PostAcceptFriendRequestViewModel
import com.paytm.pgsdk.Log
import com.paytm.pgsdk.PaytmOrder
import com.paytm.pgsdk.PaytmPGService
import com.paytm.pgsdk.PaytmPaymentTransactionCallback
import kotlinx.android.synthetic.main.fragment_wallet.*
import kotlinx.android.synthetic.main.profile_header.rlButtons
import org.json.JSONException
import org.json.JSONObject


class WalletFragment : BaseFragment(), PaytmPaymentTransactionCallback,View.OnClickListener {

    lateinit var getchecksumviewmodel: GetCheckSumViewModel
    var TAG = "WalletFragment"
    var mid = "eNnjXe00637647587210"
    var orderId="1234"
    var custid = "4321"
    var CHECKSUMHASH = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        v = inflater.inflate(R.layout.fragment_wallet, container, false)
        return v
    }

    override fun onBackTriggered() {
        home().exitApp()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI();
    }

    private fun initUI() {
        setUSerMAinDataAPIObserver()
        setGetCheckSUMRequestObserver()
        rlButtons.visibility = View.GONE
        recharge.setOnClickListener(this)
        posUserMainDataViewModel.loadData(UserInfoManager.getInstance(activity!!).getAccountId())

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
                            val Service = PaytmPGService.getStagingService()
                            val paramMap =
                                HashMap<String, String>()
                            //these are mandatory parameters
                            paramMap["MID"] = mid //MID provided by paytm
                            paramMap["ORDER_ID"] = orderId
                            paramMap["CUST_ID"] = custid
                            paramMap["CHANNEL_ID"] = "WAP"
                            paramMap["TXN_AMOUNT"] = "100"
                            paramMap["WEBSITE"] = "WEBSTAGING"
                            paramMap["CALLBACK_URL"] = "varifyurl"
                            //paramMap.put( "EMAIL" , "abc@gmail.com");   // no need
                            // paramMap.put( "MOBILE_NO" , "9144040888");  // no need
                            paramMap["CHECKSUMHASH"] = CHECKSUMHASH
                            //paramMap.put("PAYMENT_TYPE_ID" ,"CC");    // no need
                            paramMap["INDUSTRY_TYPE_ID"] = "Retail"
                            val Order = PaytmOrder(paramMap)
                            Log.e("checksum ", "param $paramMap")
                            Service.initialize(Order, null)
                            // start payment service call here
                            Service.startPaymentTransaction(
                                context, true, true,
                                this@WalletFragment
            )
                        }
                    }
                })
            }
        }
    }

    override fun onTransactionResponse(inResponse: Bundle?) {
        Log.d(TAG,"inResponse "+inResponse.toString())
    }

    override fun clientAuthenticationFailed(inErrorMessage: String?) {
        Log.d(TAG,"inErrorMessage "+inErrorMessage.toString())

    }

    override fun someUIErrorOccurred(inErrorMessage: String?) {
        Log.d(TAG,"someUIErrorOccurred "+inErrorMessage.toString())

    }

    override fun onTransactionCancel(inErrorMessage: String?, inResponse: Bundle?) {
        Log.d(TAG,"onTransactionCancel "+inErrorMessage.toString())

    }

    override fun networkNotAvailable() {
        Log.d(TAG,"networkNotAvailable ")

    }

    override fun onErrorLoadingWebPage(
        iniErrorCode: Int,
        inErrorMessage: String?,
        inFailingUrl: String?
    ) {
        Log.d(TAG,"onErrorLoadingWebPage ")

    }

    override fun onBackPressedCancelTransaction() {
        Log.d(TAG,"onBackPressedCancelTransaction ")
    }

    override fun onClick(v: View?) {
        when(v?.id) {
            R.id.recharge -> {
                var varifyurl = "https://pguat.paytm.com/paytmchecksum/paytmCallback.jsp"
                getchecksumviewmodel.loadData(varifyurl,orderId,custid)
            }
        }
    }
}
