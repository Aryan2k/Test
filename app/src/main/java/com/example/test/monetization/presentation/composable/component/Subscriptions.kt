package com.viewlift.monetization.presentation.composable.component

import android.app.Activity
import android.util.Log
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.viewlift.common.ui.AppTypography
import com.viewlift.monetization.presentation.model.PlanSettings
import com.viewlift.monetization.presentation.viewmodel.ViewPlanViewModel
import com.viewlift.network.BootStrapQuery
import com.vl.viewlift.monetization.data.model.PlanResponse

private const val TAG = "ButtonSubscribe"

@Composable
fun ButtonSubscribe(
    viewModel: ViewPlanViewModel,
    pageSettings: PlanSettings?,
    selectedPlan: PlanResponse?
) {
    var callToAction: String? = selectedPlan?.planDetails?.get(0)?.callToAction
    if (callToAction == null) {
        callToAction = pageSettings?.btnText
    }

    val isTablet = LocalContext.current.resources.getBoolean(com.viewlift.common.R.bool.isTablet)

    val buttonModifier = Modifier
        .fillMaxWidth()
        .height(40.dp)
        .padding(start = 10.dp, end = 15.dp)

    val activity = LocalContext.current as Activity
    Button(modifier = buttonModifier,
        shape = RoundedCornerShape(2.dp),
        colors = ButtonDefaults.buttonColors(backgroundColor = pageSettings?.buttonColor ?: Color.Black),
        onClick = {
            Log.e("test", "SubscribeButton: click ")
            viewModel.launchPurchaseFlow(activity, selectedPlan!!, "")
        }) {
        Text(
            text = callToAction ?: "",
            style = AppTypography.labelMediumSemiBold.copy(color = pageSettings?.titleColor ?: Color.Black)
        )
    }

}




