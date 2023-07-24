package com.viewlift.monetization.presentation.composable.component

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.size.Scale
import com.viewlift.common.label.BootstrapColors
import com.viewlift.common.ui.AppTypography
import com.viewlift.common.ui.composable.gradientBottom
import com.viewlift.common.utils.parse
import com.viewlift.monetization.R
import com.viewlift.monetization.data.model.Image
import com.viewlift.monetization.presentation.composable.BottomUi
import com.viewlift.monetization.presentation.model.PlanSettings
import com.viewlift.network.BootStrapQuery
import kotlinx.coroutines.delay

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun imageSlide(pageSettings: PlanSettings, items : List<Image>, bootstrap: BootStrapQuery.Bootstrap?) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colors.background
    ) {

        val state = rememberPagerState(){items.size}
        Column {
            val isTablet = LocalContext.current.resources.getBoolean(com.viewlift.common.R.bool.isTablet)


            Box(modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.TopStart,
            ) {
                SliderView(state, items, shadowColor = pageSettings.shadowColor)
                Column(modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .align(Alignment.BottomCenter)
                    .padding(15.dp, 20.dp)) {

                    val configuration = LocalConfiguration.current
                    val SliderTitleSpacing = (configuration.screenHeightDp/2.5).toInt()
                    Spacer(modifier = Modifier.height(SliderTitleSpacing.dp))

                    items[state.currentPage].title?.let {
                        Text(
                            text = it,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = if (isTablet) TextAlign.Center else TextAlign.Start,
                            maxLines = 2,
                            style = AppTypography.titleMedium.
                                    copy(color = pageSettings.titleColor,
                                        fontWeight = FontWeight.Normal, fontSize = if (isTablet) 40.sp else 24.sp)
                        )
                    }
                    Spacer(modifier = Modifier.padding(5.dp))
                    items[state.currentPage].description?.let {
                        Text(
                            text = it,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = if (isTablet) TextAlign.Center else TextAlign.Start,
                            style = AppTypography.bodyMedium.
                            copy(color = pageSettings.subTitleColor),
                        )
                    }
                    Spacer(modifier = Modifier.padding(10.dp))
                    DotsIndicator(
                        totalDots = items.size,
                        selectedIndex = state.currentPage
                    )
                    Spacer(modifier = Modifier.padding(5.dp))
                    if (isTablet){
                        BottomUi(pageSettings, bootstrap)
                    }
                }
            }

        }
        LaunchedEffect(key1 = state.currentPage) {
            delay(3000)
            var newPosition = state.currentPage + 1
            if (newPosition > items.size - 1) newPosition = 0
            state.scrollToPage(newPosition)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SliderView(state: PagerState,
               items: List<Image>,
               placeHolderImage : Int = R.drawable.placeholder, shadowColor : Color) {
    val imageUrl =
        remember { mutableStateOf("") }
    HorizontalPager(
        state = state,
        modifier = Modifier
            .fillMaxSize()
            .background(BootstrapColors.generalBackground.parse)
            .gradientBottom(BootstrapColors.generalBackground.parse)
    ) { page ->
        imageUrl.value = items[page].imageUrl.toString()
        val isTablet = LocalContext.current.resources.getBoolean(com.viewlift.common.R.bool.isTablet)

        Column(
            modifier = Modifier
                .fillMaxSize(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(contentAlignment = Alignment.BottomCenter,
                modifier = Modifier.fillMaxSize()) {

                val painter = rememberAsyncImagePainter(
                    ImageRequest.Builder(LocalContext.current).data(data = imageUrl.value)
                        .apply(block = fun ImageRequest.Builder.() {
                            scale(Scale.FIT)
                            placeholder(placeHolderImage)
                            error(placeHolderImage)
                        }).build()
                )
                Image(
                    painter = painter, contentDescription = "",
                    modifier = Modifier
                        .drawWithCache {
                            val gradient = Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    BootstrapColors.generalBackground.parse
                                ),
                                startY = size.height / 1.5F,
                                endY = size.height
                            )
                            onDrawWithContent {
                                drawContent()
                                drawRect(gradient, blendMode = BlendMode.Darken)
                            }
                        }
                        .fillMaxSize()
                        .align(Alignment.TopCenter),
                    contentScale = ContentScale.Crop,

                )

                if (isTablet){
                    Column {
                        Box (modifier = Modifier
                            .height(200.dp)
                            .fillMaxWidth()
                            .gradientBottom(BootstrapColors.generalBackground.parse)
                            .zIndex(1f)){
                        }
                        Box (modifier = Modifier
                            .height(400.dp)
                            .fillMaxWidth()
                            .background(BootstrapColors.generalBackground.parse)
                            .zIndex(1f)){

                        }
                    }
                }
            }
        }
    }
}


@Composable
fun DotsIndicator(
    totalDots: Int,
    selectedIndex: Int,
    selectedDotColor : Color = Color.White,
    dotColor : Color = Color.DarkGray
) {

    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        horizontalArrangement = Arrangement.Center
    ) {

        items(totalDots) { index ->
            if (index == selectedIndex) {
                Box(
                    modifier = Modifier
                        .width(30.dp)
                        .height(1.5.dp)
                        .clip(RectangleShape)
                        .background(color = selectedDotColor)
                )
            } else {
                Box(
                    modifier = Modifier
                        .width(20.dp)
                        .height(1.5.dp)
                        .clip(RectangleShape)
                        .background(color = dotColor)
                )
            }

            if (index != totalDots - 1) {
                Spacer(modifier = Modifier.padding(horizontal = 2.dp))
            }
        }
    }
}

