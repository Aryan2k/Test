package com.viewlift.monetization.presentation.composable.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.toLowerCase
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import com.viewlift.common.R
import com.viewlift.common.ui.AppTypography
import com.viewlift.common.utils.parse
import com.vl.viewlift.monetization.data.model.FeatureDetails
import com.vl.viewlift.monetization.data.model.PlanResponse

@Composable
fun PlanInfo(plan: List<PlanResponse>?, textColor : Color, bgColor : Color, onDismiss: () -> Unit) {
    Dialog(
        onDismissRequest = onDismiss,
        DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false, usePlatformDefaultWidth = false)
    ) {
        val configuration = LocalConfiguration.current
        val isTablet = LocalContext.current.resources.getBoolean(com.viewlift.common.R.bool.isTablet)

        Box(
            modifier = Modifier
                .requiredSize(configuration.screenWidthDp.dp, configuration.screenHeightDp.dp)
                .background("#EDF0F2".parse)
                .zIndex(1f)
        ) {
            Card(
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.requiredSize(configuration.screenWidthDp.dp, configuration.screenHeightDp.dp)
            ) {
                Column(
                    modifier = Modifier
                        .requiredSize(
                            configuration.screenWidthDp.dp,
                            configuration.screenHeightDp.dp
                        )
                        .background("#EDF0F2".parse)
                ) {
                    Box(modifier = Modifier
                        .fillMaxWidth()
                        .padding(15.dp)) {
                        Text(text = "Plan Details",
                            style = AppTypography.labelMediumNormal.copy(color = textColor, fontSize = if (isTablet) 28.sp else 24.sp), fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Start)

                        Icon(
                            painter = painterResource(id = R.drawable.ic_close_white),
                            contentDescription = "Close",
                            modifier = Modifier
                                .clickable {
                                    onDismiss()
                                }
                                .align(Alignment.TopEnd),
                            tint = textColor
                        )
                    }

                    LazyRow(
                        modifier = Modifier.align(Alignment.End),
                        contentPadding = PaddingValues(
                            start = 4.dp,
                            top = 4.dp,
                            end = 4.dp,
                            bottom = 4.dp
                        ),
                        content = {
                            items(plan?.size ?: 0) { index ->
                                Spacer(modifier = Modifier.width(5.dp))
                                val backgroundColor = if (index % 2 == 0) "#FF0032AE".parse else "#0D1D41".parse
                                Box(
                                    modifier = Modifier
                                        .height(95.dp)
                                        .width(if (isTablet) 180.dp else 100.dp)
                                        .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                        .background(backgroundColor)
                                        .padding(5.dp),
                                ) {
                                    Column (modifier = Modifier.padding(5.dp)) {

                                        Text(
                                            text = plan?.get(index)?.name ?: "",
                                            textAlign = TextAlign.Start,
                                            style = TextStyle(
                                                fontSize = 18.sp,
                                                color = Color.White,
                                                fontWeight = FontWeight.W700
                                            )
                                        )
                                        Spacer(modifier = Modifier.height(5.dp))

                                        val planDetails = plan?.get(index)?.planDetails?.get(0)?.recurringPaymentCurrencyCode?.plus(" ").plus(
                                            plan?.get(index)?.planDetails?.get(0)?.recurringPaymentAmount)?.plus("/").plus(plan?.get(index)?.renewalCycleType?.lowercase())

                                        Text(
                                            planDetails ?: "",
                                            maxLines = 2,
                                            style = AppTypography.labelMediumSemiBold.copy(color = Color.White)
                                        )
                                    }
                                }
                            }
                        })

                    val listOfCombinedFeatures : MutableList<FeatureDetails> = mutableListOf()

                    val planIdList: MutableList<String?> = mutableListOf()

                    plan?.forEach {plan->
                        plan.planDetails?.get(0)?.featureDetails?.forEach{feature->
                            if (feature != null) {
                                val findExistingItem = listOfCombinedFeatures.find { it.textToDisplay == feature.textToDisplay }
                                if (findExistingItem == null){
                                    listOfCombinedFeatures.add(feature)
                                }
                            }
                        }
                        planIdList.add(plan.id)
                    }

                    Column() {
                        listOfCombinedFeatures.forEachIndexed { index, featureDetails ->
                            val backgroundColor = if (index % 2 == 0) Color.White else "#EDF0F2".parse
                            MarketingListItem(singleFeature = featureDetails ,
                                              isAvailableAnnual = planAvailableInAnnual(featureDetails, plan, planIdList),
                                              isAvailableMonthly = planAvailableInMonthly(featureDetails, plan, planIdList),
                                              backgroundColor = backgroundColor)
                        }
                    }


                    Spacer(modifier = Modifier.height(15.dp))
//                    var featureDetails = plan?.planDetails?.get(0)?.featureDetails
//                    featureDetails?.forEach { featureDetail ->
//                        Row(modifier = Modifier
//                            .fillMaxWidth()
//                            .padding(horizontal = 10.dp)) {
//                            Icon(painter = painterResource(id = com.viewlift.monetization.R.drawable.baseline_check_24),
//                                contentDescription = "", tint = textColor,
//                            modifier = Modifier.align(CenterVertically))
//                            Spacer(modifier = Modifier.width(10.dp))
//                            Text(modifier = Modifier
//                                .align(CenterVertically)
//                                .padding(vertical = 15.dp),
//                                style = AppTypography.labelMediumNormal.copy(color = textColor),
//                                text = featureDetail?.textToDisplay.toString()
//                                )
//                        }
//                    }
                    Spacer(modifier = Modifier.height(15.dp))
                }
            }
        }
    }
}

fun planAvailableInAnnual(featureDetails: FeatureDetails, plan: List<PlanResponse>?, planIdList: MutableList<String?>): Boolean {
    val annualPlanId = planIdList.getOrNull(0)
    return if (annualPlanId!=null){
        val annualPlan = plan?.find { it.id == annualPlanId }
        val feature = annualPlan?.planDetails?.get(0)?.featureDetails?.find { it == featureDetails }
        feature != null
    } else {
        false
    }
}

fun planAvailableInMonthly(featureDetails: FeatureDetails, plan: List<PlanResponse>?, planIdList: MutableList<String?>): Boolean {
    val monthlyPlanId = planIdList.getOrNull(1)
    return if (monthlyPlanId!=null){
        val monthlyPlan = plan?.find { it.id == monthlyPlanId }
        val feature = monthlyPlan?.planDetails?.get(0)?.featureDetails?.find { it == featureDetails }
        feature != null
    } else {
        false
    }

}



@Composable
fun MarketingListItem(singleFeature : FeatureDetails, isAvailableAnnual: Boolean, isAvailableMonthly: Boolean, backgroundColor: Color) {
    val isTablet = LocalContext.current.resources.getBoolean(com.viewlift.common.R.bool.isTablet)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = singleFeature.textToDisplay ?: "",
            style = TextStyle(
                fontSize = 14.sp,
                fontWeight = FontWeight(600),
                color = Color.Black
            ),
            modifier = Modifier
                .weight(1.5f)
                .padding(16.dp)
        )
        Box(
            Modifier
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            if (isAvailableAnnual) {
                Icon(
                    painter = painterResource(id = R.drawable.icon_sizes),
                    contentDescription = "TickIcon",
                    tint = Color(0xFF007F06),
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Box(
            Modifier
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            if (isAvailableMonthly) {
                Icon(
                    painter = painterResource(id = R.drawable.icon_sizes),
                    contentDescription = "TickIcon",
                    tint = Color(0xFF007F06),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
