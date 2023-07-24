package com.viewlift.monetization.presentation.util

import androidx.compose.ui.graphics.Color
import com.example.test.monetization.presentation.util.PageColors.backgroundColor
import com.example.test.monetization.presentation.util.PageColors.buttonColor
import com.example.test.monetization.presentation.util.PageColors.chipBgColor
import com.example.test.monetization.presentation.util.PageColors.progressBarBackgroundColor
import com.example.test.monetization.presentation.util.PageColors.progressBarColor
import com.example.test.monetization.presentation.util.PageColors.selectedChipBgColor
import com.example.test.monetization.presentation.util.PageColors.selectedChipBorderColor
import com.example.test.monetization.presentation.util.PageColors.selectedChipTextColor
import com.example.test.monetization.presentation.util.PageColors.shadowColor
import com.example.test.monetization.presentation.util.PageColors.subTitleColor
import com.example.test.monetization.presentation.util.PageColors.titleColor
import com.viewlift.monetization.presentation.model.PlanSettings
import com.viewlift.monetization.presentation.viewmodel.ViewPlanViewModel
import com.viewlift.network.BootStrapQuery

private const val TAG = "UIUtils"
fun Color.Companion.parse(colorString: String): Color =
    Color(color = android.graphics.Color.parseColor(colorString))

fun getPageSettings(
    bootstrap: BootStrapQuery.Bootstrap?,
    settings: Any?,
    defaultBtnLabel: String, defaultTermsCondition: String,
    viewModel: ViewPlanViewModel = hiltViewModel()
): PlanSettings {

    val apiBaseUrl: String? = null
    var settingMetaData: Map<String?, String?> = emptyMap()
    if (settings != null && settings is LinkedHashMap<*, *>) {
        settingMetaData = (settings as LinkedHashMap<String?, String?>)
    }

    viewModel.updateBootstrap(bootstrap)

    if (settingMetaData["CTABgColor"] != null) {
        chipBgColor = settingMetaData["CTABgColor"]?.let { Color.parse(it) }!!
    }

    return PlanSettings(
        backgroundColor = backgroundColor,
        chipBgColor = chipBgColor,
        titleColor = titleColor,
        subTitleColor = subTitleColor,
        buttonColor = buttonColor,
        btnText = defaultBtnLabel,
        selectedChipBgColor = selectedChipBgColor,
        selectedChipBorderColor = selectedChipBorderColor,
        selectedChipTextColor = selectedChipTextColor,
        termsCondition = defaultTermsCondition,
        shadowColor = shadowColor,
        apiBaseUrl = apiBaseUrl,
        progressBarColor = progressBarColor,
        progressBarBackgroundColor = progressBarBackgroundColor
    )
}
