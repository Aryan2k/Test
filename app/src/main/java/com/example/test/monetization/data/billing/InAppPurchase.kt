package com.viewlift.monetization.data.billing

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.util.Log
import com.android.billingclient.api.*
import com.viewlift.monetization.data.model.PurchaseResult
import com.viewlift.monetization.data.model.PurchaseResultCode

private const val TAG = "BillingClientLifecycle"

interface InAppPurchaseListener {
    fun onInAppPurchaseResult(result: PurchaseResult)
}

class InAppPurchase private constructor(
    private val applicationContext: Context
) : PurchasesUpdatedListener {

    private var inAppPurchaseListener: InAppPurchaseListener? = null
    private var billingClient = BillingClient.newBuilder(applicationContext)
        .setListener(this)
        .enablePendingPurchases()
        .build()

    fun setInAppPurchaseListener(inAppPurchaseListener: InAppPurchaseListener) {
        this.inAppPurchaseListener = inAppPurchaseListener
    }

    fun launchPurchaseFlow(
        activity: Activity, userId: String,
        skuToBuy: String
    ) {
        val billingClientStateListener = object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    startPurchaseFlow(activity, userId, skuToBuy)
                } else {
                    if (inAppPurchaseListener != null)
                        inAppPurchaseListener?.onInAppPurchaseResult(
                            PurchaseResult(PurchaseResultCode.UNABLE_TO_CONNECT, null, null, -1)
                        )
                }
            }

            override fun onBillingServiceDisconnected() {
            }
        }
        if (!billingClient.isReady) {
            Log.e(TAG, "queryPurchases: BillingClient is not ready")
            billingClient.startConnection(billingClientStateListener)
        } else {
            startPurchaseFlow(activity, userId, skuToBuy)
        }
    }

    private fun startPurchaseFlow(activity: Activity, userId: String,
                                  skuToBuy: String) {
        val skuList = ArrayList<QueryProductDetailsParams.Product>()
        var productToBuy = QueryProductDetailsParams.Product.newBuilder()
            .setProductId(skuToBuy).setProductType(BillingClient.ProductType.INAPP)
            .build()
        skuList.add(productToBuy)
        val queryProductDetailsParams = QueryProductDetailsParams
            .newBuilder()
            .setProductList(skuList)
            .build()
        billingClient.queryProductDetailsAsync(queryProductDetailsParams) { billingResult,
                                                                            productDetailsList ->
            if (productDetailsList != null && productDetailsList.size == 0) {
                if (inAppPurchaseListener != null)
                    inAppPurchaseListener?.onInAppPurchaseResult(
                        PurchaseResult(
                            PurchaseResultCode.PRODUCT_DETAILS_UNAVAILABLE,
                            null,
                            null,
                            -1
                        )
                    )
            } else {
                var productDetailsParamsList: ArrayList<BillingFlowParams.ProductDetailsParams> =
                    ArrayList()
                for (productDetails in productDetailsList) {
                    if (productDetails != null) {
                        if (productDetails.productId.equals(skuToBuy, true)) {
                            var productDetailsParams = BillingFlowParams
                                .ProductDetailsParams.newBuilder()
                                .setProductDetails(productDetails).build()
                            productDetailsParamsList.add(productDetailsParams)
                        }
                    }
                }
                openPaymentScreen(activity, userId, productDetailsParamsList)
            }
        }
    }

    private fun openPaymentScreen(
        activity: Activity, userId: String,
        productDetailsList: List<BillingFlowParams.ProductDetailsParams>
    ) {
        val flowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsList)
            .setObfuscatedAccountId(userId)
            .build()
        billingClient.launchBillingFlow(activity, flowParams)
    }

    override fun onPurchasesUpdated(
        billingResult: BillingResult,
        purcahses: MutableList<Purchase>?
    ) {
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                purcahses?.get(0)?.purchaseToken?.let { consumeProduct(it) }
                purcahses?.get(0)?.originalJson.let {
                    if (inAppPurchaseListener != null)
                        inAppPurchaseListener?.onInAppPurchaseResult(
                            PurchaseResult(PurchaseResultCode.PURCHASE_DONE, it, null, -1)
                        )
                }
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> {
                if (inAppPurchaseListener != null)
                    inAppPurchaseListener?.onInAppPurchaseResult(
                        PurchaseResult(PurchaseResultCode.PURCHASE_CANCEL, null, null, -1)
                    )
            }

            BillingClient.BillingResponseCode.DEVELOPER_ERROR -> {
                if (inAppPurchaseListener != null)
                    inAppPurchaseListener?.onInAppPurchaseResult(
                        PurchaseResult(PurchaseResultCode.DEVELOPER_ERROR, null, null, -1)
                    )
            }
            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
                queryPurchases()
            }
            BillingClient.BillingResponseCode.ITEM_UNAVAILABLE -> {
                if (inAppPurchaseListener != null)
                    inAppPurchaseListener?.onInAppPurchaseResult(
                        PurchaseResult(PurchaseResultCode.ITEM_UNAVAILABLE, null, null, -1)
                    )
            }
            BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE -> {
                if (inAppPurchaseListener != null)
                    inAppPurchaseListener?.onInAppPurchaseResult(
                        PurchaseResult(PurchaseResultCode.SERVICE_UNAVAILABLE, null, null, -1)
                    )
            }
            BillingClient.BillingResponseCode.ITEM_NOT_OWNED -> {
                if (inAppPurchaseListener != null)
                    inAppPurchaseListener?.onInAppPurchaseResult(
                        PurchaseResult(PurchaseResultCode.ITEM_NOT_OWNED, null, null, -1)
                    )
            }
        }
    }

    /**
     * Query Google Play Billing for existing purchases.
     *
     * New purchases will be provided to the PurchasesUpdatedListener.
     * You still need to check the Google Play Billing API to know when purchase tokens are removed.
     */
    private fun queryPurchases() {
        val billingClientStateListener = object : BillingClientStateListener {
            override fun onBillingServiceDisconnected() {
                if (inAppPurchaseListener != null)
                    inAppPurchaseListener?.onInAppPurchaseResult(
                        PurchaseResult(PurchaseResultCode.UNABLE_TO_CONNECT, null, null, -1)
                    )
            }

            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    fetchPurchase()
                } else {
                    if (inAppPurchaseListener != null)
                        inAppPurchaseListener?.onInAppPurchaseResult(
                            PurchaseResult(PurchaseResultCode.UNABLE_TO_CONNECT, null, null, -1)
                        )
                }
            }
        }
        if (!billingClient.isReady) {
            Log.e(TAG, "queryPurchases: BillingClient is not ready")
            billingClient.startConnection(billingClientStateListener)
        } else {
            fetchPurchase()
        }
    }

    private fun fetchPurchase() {
        val purchasesResponseListener =
            PurchasesResponseListener { _, purchases ->
                for (purchase in purchases) {
                    consumeProduct(purchase.purchaseToken)
                }
                purchases?.get(0)?.originalJson.let {
                    if (inAppPurchaseListener != null)
                        inAppPurchaseListener?.onInAppPurchaseResult(
                            PurchaseResult(PurchaseResultCode.ITEM_ALREADY_OWNED, it, null, -1)
                        )
                }
            }
        billingClient.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.INAPP)
                .build(), purchasesResponseListener
        )
    }

    private fun consumeProduct(purchaseToken: String) {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    // The BillingClient is ready. You can query purchases here.
                    billingClient.consumeAsync(
                        ConsumeParams.newBuilder().setPurchaseToken(purchaseToken).build()
                    ) { billingResult, _ ->
                        billingClient.endConnection()
                        when (billingResult.responseCode) {
                            BillingClient.BillingResponseCode.OK -> {
                            }
                        }
                    }

                }
            }
            override fun onBillingServiceDisconnected() {
                if (inAppPurchaseListener != null)
                    inAppPurchaseListener?.onInAppPurchaseResult(
                        PurchaseResult(PurchaseResultCode.UNABLE_TO_CONNECT, null, null, -1)
                    )
            }
        })
    }
}