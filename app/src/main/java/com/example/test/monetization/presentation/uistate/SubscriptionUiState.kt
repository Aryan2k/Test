package com.viewlift.monetization.presentation.uistate

import android.os.Parcelable
import androidx.compose.runtime.Immutable
import com.android.billingclient.api.ProductDetails
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Immutable
@Parcelize
data class SubscriptionUiState(val isLoading: Boolean = false,
                               val isError: Boolean = false,
                               @IgnoredOnParcel val productDetails: ProductDetails? = null) : Parcelable {
    /**
     * Partial state
     *
     * @constructor Create empty Partial state
     */
    sealed class PartialState {
        object Loading : PartialState() // for simplicity: initial loading & refreshing

        /**
         * Error
         *
         * @property throwable
         * @constructor Create empty Error
         */
        data class Error(val throwable: Throwable) : PartialState()

        data class FetchedProductDetails(val productDetails: ProductDetails) : PartialState()
    }

}
