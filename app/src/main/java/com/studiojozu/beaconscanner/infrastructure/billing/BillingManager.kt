package com.studiojozu.beaconscanner.infrastructure.billing

import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.billingclient.api.*

/**
 * Created by r.mori on 2020-02-21.
 */
class BillingManager(
    context: Context,
    private val billingListener: BillingListener
) {
    interface BillingListener {
        fun onFinishSetUp(billingResult: BillingResult?)
        fun onDisconnectBillingService()
        fun onUpdatePurchase(billingResult: BillingResult, purchases: List<Purchase>?)
    }

    companion object {
        const val SKU_DONATE_100 = "donate_100"
        const val SKU_DONATE_500 = "donate_500"
        const val SKU_DONATE_1000 = "donate_1000"
    }

    private val billingClient: BillingClient

    private val purchaseUpdateListener =
        PurchasesUpdatedListener { billingResult, purchases ->
            Log.d("BillingManager", "[onPurchaseUpdate] ${billingResult.responseCode}, $purchases")
            billingListener.onUpdatePurchase(billingResult, purchases)
        }

    init {
        billingClient = BillingClient.newBuilder(context)
            .enablePendingPurchases()
            .setListener(purchaseUpdateListener)
            .build()
    }

    fun startConnection() {
        if(billingClient.isReady) {
            return
        }

        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult?) {
                // The BillingClient is ready. You can query purchases here.
                Log.d("BillingManager", "[onBillingSetupFinished] ${billingResult?.responseCode} billingClient.isReady=${billingClient.isReady}")
                billingListener.onFinishSetUp(billingResult)
            }

            override fun onBillingServiceDisconnected() {
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
                Log.d("BillingManager", "[onBillingServiceDisconnected]")
                billingListener.onDisconnectBillingService()
            }
        })
    }

    fun endConnection() {
        if (billingClient.isReady) {
            billingClient.endConnection()
        }
    }

    fun getSkuDetail(listener: (billingResult: BillingResult?, skuDetailList: List<SkuDetails>) -> Unit) {
        val skuType = BillingClient.SkuType.INAPP
        val inAppSkuList = listOf(SKU_DONATE_100, SKU_DONATE_500, SKU_DONATE_1000)
        val skuDetailsParams = SkuDetailsParams.newBuilder()
            .setType(skuType)
            .setSkusList(inAppSkuList)
            .build()

        if (billingClient.isReady) {
            Log.d("BillingManager", "[getSkuDetail] call querySkuDetailsAsync")
            billingClient.querySkuDetailsAsync(skuDetailsParams) { billingResult, skuDetailList ->
                Log.d("BillingManager", "[getSkuDetail] billingResult=${billingResult.responseCode}")
                Log.d("BillingManager", "[getSkuDetail] skuDetailList=$skuDetailList")
                listener.invoke(billingResult, skuDetailList)
            }
        } else {
            Log.d("BillingManager", "[getSkuDetail] billingClient is not ready...")
            listener.invoke(null, listOf())
        }
    }

    fun startBillingFlow(activity: Activity, skuDetails: SkuDetails): Boolean {
        val billingFlowParams = BillingFlowParams.newBuilder()
            .setSkuDetails(skuDetails)
            .build()

        return if (billingClient.isReady) {
            Log.d("BillingManager", "[startBillingFlow] call launchBillingFlow")
            billingClient.launchBillingFlow(activity, billingFlowParams)
            true
        } else {
            Log.d("BillingManager", "[startBillingFlow] billingClient is not ready...")
            false
        }
    }

    fun getPurchases(): List<Purchase>? {
        return if (billingClient.isReady) {
            Log.d("BillingManager", "[getPurchases] call queryPurchases")
            val purchaseResult = billingClient.queryPurchases(BillingClient.SkuType.INAPP)
            Log.d("BillingManager", "[getPurchases] purchaseResult=$purchaseResult")
            purchaseResult.purchasesList
        } else {
            Log.d("BillingManager", "[getPurchases] billingClient is not ready...")
            null
        }
    }

    fun getPurchaseHistory(listener: (purchaseList: List<Purchase>?) -> Unit) {
        if (billingClient.isReady) {
            val skuType = BillingClient.SkuType.INAPP

            Log.d("BillingManager", "[getPurchaseHistory] call queryPurchaseHistoryAsync")
            billingClient.queryPurchaseHistoryAsync(skuType) { billingResult, purchaseHistoryRecordList ->
                Log.d("BillingManager", "[getPurchaseHistory] billingResult=${billingResult.responseCode}")
                Log.d("BillingManager", "[getPurchaseHistory] purchaseHistoryRecordList=$purchaseHistoryRecordList")
            }
        } else {
            Log.d("BillingManager", "[getPurchaseHistory] billingClient is not ready...")
            listener.invoke(null)
        }
    }

    fun consume(purchaseToken: String, listener: (billingResult: BillingResult?, purchaseToken: String?) -> Unit) {
        if (billingClient.isReady) {
            val consumeParams = ConsumeParams.newBuilder()
                .setPurchaseToken(purchaseToken)
                .build()

            Log.d("BillingManager", "[consume] call consumeAsync")
            billingClient.consumeAsync(consumeParams) { billingResult, purchaseTokenResult ->
                Log.d("BillingManager", "[consume] billingResult=${billingResult.responseCode}, purchaseToken=$purchaseTokenResult")
                listener.invoke(billingResult, purchaseToken)
            }
        } else {
            Log.d("BillingManager", "[consume] billingClient is not ready...")
            listener.invoke(null, null)
        }
    }
}