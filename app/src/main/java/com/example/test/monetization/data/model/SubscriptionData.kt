package com.viewlift.monetization.data.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Keep
@Serializable
data class PurchaseDetails(
    @SerializedName("orderId") val orderId: String,
    @SerializedName("packageName") val packageName: String,
    @SerializedName("productId") val productId: String,
    @SerializedName("purchaseTime") val purchaseTime: String,
    @SerializedName("purchaseState") val purchaseState: String,
    @SerializedName("purchaseToken") val purchaseToken: String,
    @SerializedName("quantity") val quantity: Int,
    @SerializedName("autoRenewing") val autoRenewing: String,
    @SerializedName("acknowledged") val acknowledged: Boolean,
    @SerializedName("products") var products: List<String?>? = null,
    @SerializedName("accountIdentifier") var accountIdentifier: AccountIdentifier? = null,
    @SerializedName("signature") var signature: String? = null
)

@Keep
@Serializable
data class AccountIdentifier(@SerializedName("obfuscatedAccountId") val obfuscatedAccountId: String? = null,
                             @SerializedName("obfuscatedProfileId") val obfuscatedProfileId: String? = null)
@Keep
@Serializable
data class PlanOffer(
    @SerializedName("offerId") val offerId: String? = null,
    @SerializedName("offerToken") val offerToken: String? = null,
    @SerializedName("basePlanId") val basePlanId: String? = null,
    @SerializedName("offerTag") val offerTags: List<String?>? = null,
    @SerializedName("pricingPhases") val pricingPhases: List<PricingPhase?>? = null
)

@Keep
@Serializable
data class PricingPhase(
    @SerializedName("billingCycleCount") val billingCycleCount: Int = 0,
    @SerializedName("formattedPrice") val formattedPrice: String? = null,
    @SerializedName("priceAmountMicros") val priceAmountMicros: Long = 0,
    @SerializedName("priceCurrencyCode") val priceCurrencyCode: String? = null,
    @SerializedName("billingPeriod") val billingPeriod: String? = null,
    @SerializedName("recurrenceMode") val recurrenceMode: Int = 0
)
