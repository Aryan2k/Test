package com.viewlift.monetization.presentation.di

import com.viewlift.monetization.presentation.uistate.SubscriptionUiState
import com.viewlift.monetization.presentation.uistate.ViewPlanUiState
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
object ViewPlanViewModelModule {

    @Provides
    fun provideViewPlanInitialUiState() : ViewPlanUiState = ViewPlanUiState(
        false)

    @Provides
    fun provideSubscriptionInitialUiState() : SubscriptionUiState = SubscriptionUiState(
        false, false, null)
}

