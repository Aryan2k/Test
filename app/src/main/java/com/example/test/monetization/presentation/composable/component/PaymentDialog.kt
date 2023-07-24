package com.viewlift.monetization.presentation.composable.component

import android.app.Activity
import android.content.Intent
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.android.billingclient.api.BillingClient
import com.viewlift.common.ui.composable.ShowCustomDialog
import com.viewlift.common.utils.ViewPlanNavType
import com.viewlift.monetization.R
import com.viewlift.monetization.data.billing.Constants
import com.viewlift.monetization.presentation.model.BillingResModel
import com.viewlift.monetization.presentation.util.parse
import com.viewlift.monetization.presentation.viewmodel.ViewPlanViewModel
import com.viewlift.network.BootStrapQuery

private const val TAG = "PaymentDialog"
@Composable
fun PaymentDialog(
    viewModel: ViewPlanViewModel,
    dialogData : BillingResModel,
) {

    val successOrFailure = if(dialogData.isSuccess) "Success!" else "Failed!"
    val iconRes =
        if (dialogData.isSuccess) painterResource(id = com.viewlift.common.R.drawable.ic_check) else painterResource(
            id = com.viewlift.common.R.drawable.ic_error
        )
    ShowCustomDialog(
        title = successOrFailure,
        message = dialogData.body,
        iconClose = painterResource(id = com.viewlift.common.R.drawable.ic_close_black),
        iconDialogType = iconRes
    ) { isClosed ->
        if (isClosed) {
            if (viewModel.getNavType() == ViewPlanNavType.ONBOARD_FLOW)
                viewModel.onOkPressed()
            else
                viewModel.popBackStack()
        }
    }

}

@Composable
fun createDialogData(
    purchaseRes: Int,
    bootstrap: BootStrapQuery.Bootstrap?
): BillingResModel? {
    var dialogData: BillingResModel? = null
    var title = bootstrap?.appcmsMain?.genericMessages?.subscriptionAlertTitle
    if (title == null) {
        title = stringResource(id = R.string.payment_dialog_title)
    }
    val btnLabel = stringResource(id = R.string.payment_dialog_btn)
    var isSuccess = false
    var body: String? = null
    if (purchaseRes != ViewPlanViewModel.DEFAULT_DIALOG_STATE) {
        when (purchaseRes) {
            BillingClient.BillingResponseCode.OK -> {
                body = stringResource(id = R.string.payment_dialog_body)
                isSuccess = true
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> {
                body =
                    stringResource(id = R.string.subscription_billing_response_result_user_canceled)
                isSuccess = false
            }
            BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE -> {
                body =
                    stringResource(id = R.string.subscription_billing_response_result_service_unavailable)
                isSuccess = false
            }
            BillingClient.BillingResponseCode.BILLING_UNAVAILABLE -> {
                addGoogleAccountToDevice()
                isSuccess = false
            }
            BillingClient.BillingResponseCode.ITEM_UNAVAILABLE  -> {
                body =
                    stringResource(id = R.string.subscription_billing_response_result_item_unavailable)
                isSuccess = false
            }
            BillingClient.BillingResponseCode.DEVELOPER_ERROR  -> {
                body =
                    stringResource(id = R.string.subscription_billing_response_result_developer_error)
                isSuccess = false
            }
            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
                body = stringResource(id = R.string.subscription_billing_response_item_already_purchased)
                isSuccess = true
            }
            BillingClient.BillingResponseCode.ITEM_NOT_OWNED -> {
                body = stringResource(id = R.string.subscription_billing_response_item_not_owned)
                isSuccess = false
            }
            BillingClient.BillingResponseCode.ERROR -> {
                var message = stringResource(id = R.string.unable_to_create_subscription)
                if (bootstrap?.appcmsMain?.genericMessages?.strErrUpdateSubscriptionStatus != null) {
                    message =
                        bootstrap.appcmsMain?.genericMessages?.strErrUpdateSubscriptionStatus!!
                }
                body = message
            }
        }
        if (body != null) {
            dialogData = BillingResModel(
                title = title,
                body = body,
                isSuccess = isSuccess,
                btnLabel = btnLabel
            )
        }
    }
    return dialogData
}

@Composable
private fun addGoogleAccountToDevice() {
    val activity = LocalContext.current as Activity
    val addAccountIntent = Intent(Settings.ACTION_ADD_ACCOUNT)
        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    addAccountIntent.putExtra(Settings.EXTRA_ACCOUNT_TYPES, arrayOf("com.google"))
    activity.startActivityForResult(
        addAccountIntent,
        Constants.ADD_GOOGLE_ACCOUNT_TO_DEVICE_REQUEST_CODE
    )
}
