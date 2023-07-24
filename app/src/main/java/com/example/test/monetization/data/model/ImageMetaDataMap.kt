package com.viewlift.monetization.data.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Keep
@Serializable
data class Image(
    @SerializedName("title") val title: String? = null,
    @SerializedName("subTitle") val subTitle: String? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("imageUrl") val imageUrl: String? = null,
    @SerializedName("id") val id: String? = null,
)