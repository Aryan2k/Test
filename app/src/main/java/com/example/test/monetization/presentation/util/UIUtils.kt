package com.viewlift.monetization.presentation.util

import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.core.graphics.toColorInt
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

    var btnText = defaultBtnLabel
    var apiBaseUrl: String? = null
    var settingMetaData: Map<String?, String?> = emptyMap()
    if (settings != null && settings is LinkedHashMap<*, *>) {
        settingMetaData = (settings as LinkedHashMap<String?, String?>)
    }
    bootstrap?.appcmsMain?.brand?.general?.backgroundColor?.toColorInt()?.let {
        backgroundColor = Color(it)
    }
    if (settingMetaData["CTABgColor"] != null) {
        chipBgColor = settingMetaData["CTABgColor"]?.let { Color.parse(it) }!!
    }

    bootstrap?.appcmsMain?.brand?.cta?.secondary?.styleAttributesWithBorderParts?.backgroundColor?.toColorInt()
        ?.let {
            chipBgColor = Color(it)
        }

    bootstrap?.appcmsMain?.brand?.general?.pageTitleColor?.toColorInt()?.let {
        titleColor = Color(it)
    }
    bootstrap?.appcmsMain?.brand?.general?.textColor?.toColorInt()?.let {
        subTitleColor = Color(it)
    }
    bootstrap?.appcmsMain?.brand?.general?.boxShadow?.toColorInt()?.let {
        shadowColor = Color(it)
    }
    bootstrap?.appcmsMain?.brand?.cta?.primary?.styleAttributesWithBorderParts?.backgroundColor?.toColorInt()
        ?.let {
            buttonColor = Color(it)
        }
    bootstrap?.appcmsMain?.brand?.general?.skeletonColor?.toColorInt()?.let {
        selectedChipBgColor = Color(it)
    }
    bootstrap?.appcmsMain?.brand?.cta?.primary?.styleAttributesWithBorderParts?.backgroundColor?.toColorInt()
        ?.let {
            selectedChipBorderColor = Color(it)
        }
    bootstrap?.appcmsMain?.brand?.footer?.textColor?.toColorInt()?.let {
        selectedChipTextColor = Color(it)
    }
    bootstrap?.appcmsPlatform?.subscriptionFlowContent?.subscriptionFlowContent?.subscriptionButtonText?.let {
        btnText = it
    }
    bootstrap?.appcmsMain?.apiBaseUrl.let {
        apiBaseUrl = it
    }
    bootstrap?.appcmsMain?.brand?.player?.progressBarColor?.toColorInt()?.let {
        progressBarColor = Color(it)
    }
    bootstrap?.appcmsMain?.brand?.player?.progressBarBackgroundColor?.toColorInt()?.let {
        progressBarBackgroundColor = Color(it)
    }

    Log.e("aryan", "aryan")

    return PlanSettings(
        backgroundColor = backgroundColor,
        chipBgColor = chipBgColor,
        titleColor = titleColor,
        subTitleColor = subTitleColor,
        buttonColor = buttonColor,
        btnText = btnText,
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
