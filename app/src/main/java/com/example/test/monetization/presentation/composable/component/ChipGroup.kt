package com.viewlift.monetization.presentation.composable.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.viewlift.monetization.presentation.model.PlanSettings
import com.vl.viewlift.monetization.data.model.PlanResponse

@Composable
fun ChipGroup(
    chipItems: List<PlanResponse?>,
    selectedChip: PlanResponse? = null,
    titleColor : Color = Color.White,
    selectedChipTextColor : Color = Color.Black,
    onSelectedChanged: (PlanResponse?) -> Unit = {},
    onPlanInfoClick : () -> Unit = {},
    pageSettings: PlanSettings? = null
) {

    val configuration = LocalConfiguration.current

    val cardHeight = 230

    val listOfPlanHeight = cardHeight * chipItems.size

    Box(
        modifier = Modifier.height(listOfPlanHeight.dp).width(configuration.screenWidthDp.dp)) {
        LazyColumn(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items(chipItems) { item ->
                Chip(onChecked = {
                    onSelectedChanged(item)
                }, item = item,
                    isSelected = selectedChip == item,
                    cardHeight = cardHeight,
                    pageSettings = pageSettings,
                    onPlanInfoClick = onPlanInfoClick)
                Spacer(modifier = Modifier.padding(4.dp))
            }
        }
    }
}
