package com.studiojozu.beaconscanner

import android.os.Bundle
import android.view.View
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.SkuDetails
import com.studiojozu.beaconscanner.infrastructure.billing.BillingManager
import com.studiojozu.beaconscanner.infrastructure.billing.BillingManager.Companion.SKU_DONATE_100
import com.studiojozu.beaconscanner.infrastructure.billing.BillingManager.Companion.SKU_DONATE_1000
import com.studiojozu.beaconscanner.infrastructure.billing.BillingManager.Companion.SKU_DONATE_500
import kotlinx.android.synthetic.main.activity_donate.*
import java.util.concurrent.CountDownLatch

/**
 * Created by r.mori on 2020-02-21.
 * Copyright (c) 2020 rei-frontier. All rights reserved.
 */
class DonateActivity : AppCompatActivity(), BillingManager.BillingListener {
    private lateinit var billingManager: BillingManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_donate)
        viewLoading.visibility = View.GONE

        billingManager = BillingManager(this, this)
        billingManager.startConnection()

        viewDonate100.setOnClickListener {
            (it.tag as? SkuDetails)?.let { skuDetails ->
                billingManager.startBillingFlow(this, skuDetails)
            }
        }
        viewDonate500.setOnClickListener {
            (it.tag as? SkuDetails)?.let { skuDetails ->
                billingManager.startBillingFlow(this, skuDetails)
            }
        }
        viewDonate1000.setOnClickListener {
            (it.tag as? SkuDetails)?.let { skuDetails ->
                billingManager.startBillingFlow(this, skuDetails)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        billingManager.endConnection()
    }

    override fun onFinishSetUp(finishResult: BillingResult?) {
        if (finishResult?.responseCode == BillingClient.BillingResponseCode.OK) {
            billingManager.getSkuDetail { billingResult, skuDetailList ->
                if (billingResult?.responseCode == BillingClient.BillingResponseCode.OK) {
                    skuDetailList.forEach { skuDetail ->
                        when (skuDetail.sku) {
                            SKU_DONATE_100 -> {
                                viewDonate100.isEnabled = true
                                viewDonate100.tag = skuDetail
                            }
                            SKU_DONATE_500 -> {
                                viewDonate500.isEnabled = true
                                viewDonate500.tag = skuDetail
                            }
                            SKU_DONATE_1000 -> {
                                viewDonate1000.isEnabled = true
                                viewDonate1000.tag = skuDetail
                            }
                        }
                    }

                    billingManager.getPurchases()
                    billingManager.getPurchaseHistory { }
                } else {
                    showMessage(R.string.cannot_start_billing_service, true)
                }
            }
        } else {
            showMessage(R.string.cannot_start_billing_service, true)
        }
    }

    override fun onDisconnectBillingService() {
        billingManager.startConnection()
    }

    override fun onUpdatePurchase(billingResult: BillingResult, purchases: List<Purchase>?) {
        if (purchases.isNullOrEmpty()) {
            showMessage(R.string.failed_purchase_update, false)
            hideLoading()
            return
        }
        if (billingResult.responseCode != BillingClient.BillingResponseCode.OK) {
            showMessage(R.string.failed_purchase_update, false)
            hideLoading()
            return
        }

        showLoading()
        val countDownLatch = CountDownLatch(purchases.count())
        var failed = false
        purchases.forEach { purchase ->
            billingManager.consume(purchase.purchaseToken) { consumeResult, _ ->
                if (consumeResult?.responseCode == BillingClient.BillingResponseCode.OK) {
                    // do nothing.
                } else {
                    failed = true
                }
                countDownLatch.countDown()
            }
        }

        countDownLatch.await()
        hideLoading()
        if (failed) {
            showMessage(R.string.failed_purchase_update, false)
        } else {
            showMessage(R.string.success_donate_message, false)
        }
    }

    private fun showMessage(@StringRes messageId: Int, needFinish: Boolean) {
        AlertDialog.Builder(this)
            .setMessage(messageId)
            .setPositiveButton(android.R.string.ok) { _, _ -> if (needFinish) finish() }
            .create()
            .show()
    }

    private fun showLoading() {
        viewLoading.visibility = View.VISIBLE
    }

    private fun hideLoading() {
        viewLoading.visibility = View.GONE
    }
}