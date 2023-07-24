package com.viewlift.monetization.presentation.composable.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import com.viewlift.common.ui.composable.CustomProgressbar
import com.viewlift.common.utils.parse

@Composable
fun ProgressDialog(color : Color, bgColor : Color) {
    Dialog(
        onDismissRequest = {  },
        DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Box(
            contentAlignment= Center,
            modifier = Modifier
                .size(100.dp)
                .background("#0C1D40".parse, shape = RoundedCornerShape(8.dp))
        ) {
            CustomProgressbar(modifier = Modifier
                .padding(8.dp))
        }
    }
}