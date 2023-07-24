package com.viewlift.monetization.presentation.model

data class BillingResModel(val title : String,
                           val body : String,
                           val isSuccess : Boolean = false,
                           val btnLabel : String)
