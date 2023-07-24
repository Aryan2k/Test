package com.viewlift.monetization.presentation.model

import androidx.compose.ui.graphics.Color
import java.util.*

class PlanSettings constructor(
    val backgroundColor: Color,
    val chipBgColor : Color,
    val titleColor: Color,
    val subTitleColor: Color,
    val buttonColor: Color,
    val btnText: String,
    val selectedChipBgColor : Color,
    val selectedChipBorderColor : Color,
    val selectedChipTextColor : Color,
    val termsCondition : String,
    val shadowColor : Color,
    val apiBaseUrl : String? = null,
    var progressBarColor : Color,
    var progressBarBackgroundColor : Color
)
