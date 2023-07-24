package com.viewlift.monetization.presentation.composable.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import com.viewlift.monetization.presentation.intent.ViewPlanIntent
import com.viewlift.monetization.presentation.viewmodel.ViewPlanViewModel

@Composable
fun RestorePurchase(viewModel : ViewPlanViewModel, text1st: String, text2nd: String, textColor: Color) {
    //val purchase by viewModel.purchase.collectAsStateWithLifecycle()
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            textAlign = TextAlign.Center,
            text = text1st,
            color = textColor
        )
        Button(
            onClick = {
                viewModel.restorePurchase()
            },
            colors = ButtonDefaults.buttonColors(backgroundColor = Color.Transparent)
        ) {
            Text(
                textAlign = TextAlign.Center,
                text = text2nd,
                style = TextStyle(textDecoration = TextDecoration.Underline),
                color = textColor
            )
        }
    }

}