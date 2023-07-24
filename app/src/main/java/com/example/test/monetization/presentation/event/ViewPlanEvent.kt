package com.viewlift.monetization.presentation.event

sealed class ViewPlanEvent {

    /**
     * Initialisation completed
     *
     * @property uri
     * @constructor Create empty Initialisation completed
     */
    data class InitialisationCompleted(val uri: String) : ViewPlanEvent()
}