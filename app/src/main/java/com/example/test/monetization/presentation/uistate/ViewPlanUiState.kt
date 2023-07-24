package com.viewlift.monetization.presentation.uistate

import android.os.Parcelable
import androidx.compose.runtime.Immutable
import kotlinx.parcelize.Parcelize

@Immutable
@Parcelize
data class ViewPlanUiState(val isLoading: Boolean = false) : Parcelable {
    /**
     * Partial state
     *
     * @constructor Create empty Partial state
     */
    sealed class PartialState {
        data class Loading(val isLoading : Boolean) : PartialState() // for simplicity: initial loading & refreshing
    }

}
