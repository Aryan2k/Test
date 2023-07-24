package com.viewlift.monetization.presentation.composable.component

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Transparent
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.toLowerCase
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.viewlift.common.ui.AppTypography
import com.viewlift.common.ui.TriangleShape
import com.viewlift.common.utils.parse
import com.viewlift.core.extensions.collectAsStateWithLifecycle
import com.viewlift.monetization.R
import com.viewlift.monetization.presentation.model.PlanSettings
import com.viewlift.monetization.presentation.viewmodel.ViewPlanViewModel
import com.vl.viewlift.monetization.data.model.FeatureDetails
import com.vl.viewlift.monetization.data.model.PlanResponse

@Composable
fun Chip(
    onChecked: (PlanResponse?) -> Unit,
    item: PlanResponse?,
    isSelected: Boolean = false,
    cardHeight: Int,
    onPlanInfoClick : () -> Unit,
    pageSettings: PlanSettings? = null,
    viewModel: ViewPlanViewModel = hiltViewModel()
) {
    val shape = RoundedCornerShape(4.dp)
//    val textColor = if (isSelected) selectedTextColor else textColor

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(cardHeight.dp)
            .background(
                color = Color.White,
                shape = shape
            )
            .clip(shape = shape)
            .toggleable(
                value = isSelected,
                onValueChange = {
                    onChecked(item)
                })
    ) {
        var planDetails = item?.planDetails?.get(0)
        Column(
            modifier = Modifier
                .padding(start = 10.dp, bottom = 10.dp)
        ) {
            Row (Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start) {
                item?.name?.let {
                    Text(
                        it,
                        modifier = Modifier.padding(top = 15.dp, start = 8.dp),
                        style = AppTypography.labelMediumNormal.
                        copy(color = "#FF0032AE".parse, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            val combinedPrice = planDetails?.recurringPaymentCurrencyCode?.plus(" ")
                ?.plus(planDetails?.recurringPaymentAmount)?.plus("/${item?.renewalCycleType?.toLowerCase(Locale.current)}")

            Row(modifier = Modifier.fillMaxWidth().padding(start = 8.dp)) {
                Text(
                    combinedPrice ?: "",
                    maxLines = 1,
                    style = AppTypography.buttonTextExtraLarge.
                    copy(color = Color.Black)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Icon(painter = painterResource(id = R.drawable.baseline_info_24),
                    tint = Color.Black,
                    contentDescription = "",
                    modifier = Modifier.clickable {
                        onPlanInfoClick()
                    })
            }
            Spacer(modifier = Modifier.height(10.dp))
            if (planDetails?.featureDetails != null && planDetails?.featureDetails?.isNotEmpty() == true) {
                val take3Features = if (planDetails.featureDetails!!.size > 3) {
                    planDetails?.featureDetails!!.take(3)
                } else {
                    planDetails?.featureDetails!!
                }

                for (featureDetail in take3Features) {
                    ItemListUi(featureDetail?.textToDisplay)
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            val filteredPlans by viewModel.filteredPlans.collectAsStateWithLifecycle()

            val chipRememberOneState = viewModel.selectedPlanIndex.collectAsStateWithLifecycle()
            var selectedPlan = filteredPlans[chipRememberOneState.value]
            val isBillingConnected by viewModel.billingConnectionState.observeAsState()
            if (isBillingConnected == true) {
                ButtonSubscribe(
                    viewModel = viewModel,
                    pageSettings = pageSettings,
                    selectedPlan = selectedPlan
                )
            }
        }
    }
}

private fun planDescriptions(featureDetails: List<FeatureDetails?>?): String {
    var details = StringBuilder()
    if (featureDetails != null && featureDetails?.isNotEmpty() == true) {
        for (featureDetail in featureDetails) {
            details.append(featureDetail?.textToDisplay).append("\n")
        }
    }
    return details.toString()
}

@Composable
fun ItemListUi(title: String?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 10.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.Start)
    ) {
        androidx.compose.material3.Icon(
            painter = painterResource(id = com.viewlift.common.R.drawable.icon_sizes),
            contentDescription = "TickIcon",
            tint = Color(0xFF007F06),
            modifier = Modifier.size(24.dp)
        )
        androidx.compose.material3.Text(
            text = title ?: "",
            style = TextStyle(fontSize = 14.sp, color = Color(0xFF07132D))
        )
    }
}
