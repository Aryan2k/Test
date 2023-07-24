package com.viewlift.monetization.presentation.composable

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.test.R
import com.viewlift.common.ui.AppTypography
import com.viewlift.common.utils.ViewPlanNavType
import com.viewlift.core.extensions.collectAsStateWithLifecycle
import com.viewlift.monetization.R
import com.viewlift.monetization.data.model.Image
import com.viewlift.monetization.presentation.composable.component.*
import com.viewlift.monetization.presentation.model.PlanSettings
import com.viewlift.monetization.presentation.util.getPageSettings
import com.viewlift.monetization.presentation.viewmodel.ViewPlanViewModel
import com.viewlift.network.BootStrapQuery
import com.viewlift.network.PageQuery
import com.viewlift.network.data.utils.LocalisedStrings
import com.vl.viewlift.monetization.data.model.PlanResponse
import kotlinx.coroutines.flow.MutableStateFlow
import timber.log.Timber


@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun ViewPlanScreen(
    topBarState: MutableStateFlow<Boolean>?,
    viewPlanModule: PageQuery.Module? = null,
    viewModel: ViewPlanViewModel = hiltViewModel()
) {
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val screenWidth = configuration.screenWidthDp.dp
    val bootstrap by remember { mutableStateOf<BootStrapQuery.Bootstrap?>(value = null) }
    var imageList: List<Image> = mutableListOf()
    try {
        val metadataMap = viewPlanModule?.onViewPlanModule?.metadataMap as Map<String, String>
        viewModel.setPageId(viewPlanModule?.id)
        imageList = viewModel.getImageMetaData(metadataMap)
    } catch (e: Exception) {
        e.printStackTrace()
    }
    val settings = viewPlanModule?.onViewPlanModule?.layout?.layout?.settings
    val pageSettings =
        getPageSettings(
            bootstrap,
            settings,
            stringResource(id = R.string.subscribe),
            stringResource(id = R.string.view_plan_agrrement)
        )
    topBarState?.value = false
    val showPlanInfo by viewModel.showPlanInfo.collectAsStateWithLifecycle()
    val isTablet = LocalContext.current.resources.getBoolean(com.viewlift.common.R.bool.isTablet)
    Column(
        horizontalAlignment = Alignment.End,
        modifier = Modifier
            .width(screenWidth)
            .height(screenHeight)
            .verticalScroll(rememberScrollState())
    ) {
        if (imageList.isNotEmpty()) {
            val h1 = if (isTablet)  screenHeight else 3 * screenHeight / 5
            Box(modifier = Modifier.height(h1)) {
                imageSlide(pageSettings, imageList!!, bootstrap)
//                if (!isTablet){
//                    Button(
//                        modifier = Modifier
//                            .align(Alignment.TopEnd)
//                            .padding(top = 25.dp),
//                        colors = ButtonDefaults.buttonColors(backgroundColor = Color.Transparent),
//                        onClick = {
//                            viewModel.navigateToHome()
//                        }
//                    ) {
//                        Icon(
//                            painter = painterResource(id = com.viewlift.common.R.drawable.icon_close),
//                            tint = Color.Unspecified,
//                            contentDescription = "Close Button",
//                            modifier = Modifier.size(16.dp,16.dp)
//                        )
//                    }
//                }
            }

        }
        if (!isTablet){
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
            ) {
                BottomUi(pageSettings, bootstrap)
            }
        }

        if (showPlanInfo && viewModel.selectedPlanInfo != null) {
            val selectedPlan: List<PlanResponse>? = viewModel.selectedPlanInfo
            PlanInfo(selectedPlan, textColor = pageSettings.selectedChipTextColor, bgColor = pageSettings.selectedChipBgColor) {
                viewModel.showPlanInfo.value = false
            }
        }
    }
//    LaunchedEffect(Unit) {
//        // execute suspending function in provided scope closure
//        bootstrap = viewModel.getBootstrapData()
//        // TODO : add logic to check if tve is enabled and user comes from onboarding flow
//        if (viewModel.bootstrapRepository.getCachedFeatures() != null
//            && viewModel.bootstrapRepository.getCachedFeatures()?.tve_login_enabled == true
//            && viewModel.appDataRepository.getTveUserId().isNullOrEmpty() && viewModel.getNavType() == ViewPlanNavType.ONBOARD_FLOW) {
//            viewModel.navigationManager.navigate(object : NavigationCommand {
//                override val destination = NavigationDestination.TveInitView.route
//            })
//        }
//    }

}

@Composable
fun BottomUi(pageSettings: PlanSettings, bootstrap : BootStrapQuery.Bootstrap?, viewModel: ViewPlanViewModel = hiltViewModel()) {
    val filteredPlans by viewModel.filteredPlans.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    if (isLoading) {
        ProgressDialog(
            color = pageSettings.progressBarColor,
            bgColor = pageSettings.backgroundColor,
        )
    }
    Column(modifier = Modifier.padding(horizontal = 10.dp),
           horizontalAlignment = Alignment.CenterHorizontally) {
        if (filteredPlans.isNotEmpty()) {
            AddPlans(
                viewModel = viewModel,
                filteredPlans = filteredPlans,
                pageSettings
            )

            Spacer(modifier = Modifier.height(10.dp))

        } else if (!isLoading && filteredPlans.isEmpty()) {
            val msg = LocalisedStrings.getPlanInCountryNotFound(context = LocalContext.current)
            if (msg != null) {
                Text(modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp),
                    text = msg,
                    style = AppTypography.titleMedium.copy(color = pageSettings.titleColor, textAlign = TextAlign.Center),
                )
            }
        }
        Text(
            text = "Skip",
            style = AppTypography.labelMediumSemiBold.copy(
                color = Color.White,
                textDecoration = TextDecoration.Underline
            ),
            textAlign = TextAlign.Center,
            modifier = Modifier.clickable {
                if (viewModel.getNavType() == ViewPlanNavType.ONBOARD_FLOW)
                    viewModel.navigateToHome()
                else
                    viewModel.popBackStack()
            }
        )
        AddTermsCondition(
            text = pageSettings.termsCondition,
            textColor = pageSettings.titleColor
        )

        val dialogState by viewModel.dialogState.collectAsStateWithLifecycle()
        Timber.e("dialogState.value--------------------: $dialogState")
        val dialogData = createDialogData(purchaseRes = dialogState, bootstrap)
        if (dialogData != null) {
            PaymentDialog(
                dialogData = dialogData,
                viewModel = viewModel
            )
        }
    }
}

@Composable
fun AddPlans(
    viewModel: ViewPlanViewModel,
    filteredPlans : List<PlanResponse>,
    pageSettings: PlanSettings
) {
    val selectedChipTextColor = pageSettings.selectedChipTextColor

    val chipRememberOneState = viewModel.selectedPlanIndex.collectAsStateWithLifecycle()
    filteredPlans?.let {
        ChipGroup(chipItems = it,
            selectedChip = filteredPlans[chipRememberOneState.value],
            selectedChipTextColor = selectedChipTextColor,
            onSelectedChanged = {
                val newIndex = filteredPlans.indexOf(it)
                viewModel.selectedPlanIndex.value = newIndex
                viewModel.sendPlanSelectionBeacon(it)
            },
            onPlanInfoClick = {
                viewModel.selectedPlanInfo = filteredPlans
                viewModel.showPlanInfo.value = true
            },
            pageSettings = pageSettings)
    }
}

@Composable
fun AddTermsCondition(text: String, textColor: Color) {
    Text(
        modifier = Modifier.padding(top = 20.dp, bottom = 10.dp),
        text = text,
        style = AppTypography.bodySmall.
        copy(color = textColor, fontSize = 10.sp)
    )
}






