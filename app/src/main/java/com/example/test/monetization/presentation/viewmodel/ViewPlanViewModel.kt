package com.viewlift.monetization.presentation.viewmodel

import android.app.Activity
import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavOptions
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.Purchase
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import com.viewlift.analytics.AnalyticsEventsKey
import com.viewlift.analytics.EventTrackingSDK
import com.viewlift.analytics.data.remote.model.request.BeaconRequest
import com.viewlift.analytics.events.beacon.BeaconEventType
import com.viewlift.analytics.events.beacon.EventData
import com.viewlift.common.utils.ViewPlanNavType
import com.viewlift.common.utils.getEnvironmentName
import com.viewlift.core.BuildConfig
import com.viewlift.core.base.BaseViewModel
import com.viewlift.core.data.AppDataRepository
import com.viewlift.core.navigation.NavigationCommand
import com.viewlift.core.navigation.NavigationDestination
import com.viewlift.core.navigation.NavigationManager
import com.viewlift.core.utils.EventBus
import com.viewlift.core.utils.NavMap
import com.viewlift.core.utils.Utils
import com.viewlift.monetization.data.model.Image
import com.viewlift.monetization.presentation.event.ViewPlanEvent
import com.viewlift.monetization.presentation.intent.ViewPlanIntent
import com.viewlift.monetization.presentation.uistate.ViewPlanUiState
import com.viewlift.monetization.presentation.uistate.ViewPlanUiState.PartialState
import com.viewlift.monetization.presentation.uistate.ViewPlanUiState.PartialState.Loading
import com.viewlift.network.BootStrapQuery
import com.viewlift.network.data.remote.model.response.ContentDatum
import com.viewlift.network.domain.repository.BootstrapRepository
import com.viewlift.network.domain.usecase.UserProfileUseCase
import com.vl.viewlift.monetization.BillingListener
import com.vl.viewlift.monetization.data.model.PlanResponse
import com.vl.viewlift.monetization.data.model.TvodPurchaseResponse
import com.vl.viewlift.monetization.domain.manager.VLMonetization
import com.vl.viewlift.monetization.domain.model.QueryData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Calendar
import javax.inject.Inject

private val DEVICE = "android_phone"
private const val MONETIZATION_MODEL_SVOD = "SVOD"

@HiltViewModel
class
ViewPlanViewModel @Inject constructor(
    private val application: Application,
    val bootstrapRepository: BootstrapRepository,
    val appDataRepository: AppDataRepository,
    val navigationManager: NavigationManager,
    private val eventBus: EventBus,
    private val eventTrackingSDK: EventTrackingSDK,
    private val savedStateHandle: SavedStateHandle,
    private val userProfileUseCase: UserProfileUseCase,
    viewPlanUiState: ViewPlanUiState
) : BaseViewModel<ViewPlanUiState, PartialState, ViewPlanEvent, ViewPlanIntent>(
    savedStateHandle,
    viewPlanUiState
) {

    private val _billingConnectionState = MutableLiveData(false)
    val billingConnectionState: LiveData<Boolean> = _billingConnectionState

    val filteredPlans = MutableStateFlow<List<PlanResponse>>(emptyList())
    private var planToPurchase: PlanResponse? = null
    val selectedPlanIndex: MutableStateFlow<Int> = MutableStateFlow(0)
    val dialogState: MutableStateFlow<Int> = MutableStateFlow(DEFAULT_DIALOG_STATE)
    val showPlanInfo: MutableStateFlow<Boolean> = MutableStateFlow(false)
    var selectedPlanInfo: List<PlanResponse>? = null
    private var userId: String? = null
    private var userName: String? = null
    private var phoneNumber: String? = null
    private var email: String? = null
    private var isUserSubscribed: Boolean? = false
    private var bootstrapData: BootStrapQuery.Bootstrap? = null
    private var apiBaseUrl: String? = null
    private var site: String = ""
    private var authToken: String? = null
    private var pageId: String? = null
    val isLoading: MutableStateFlow<Boolean> = MutableStateFlow(false)
    private var vlMonetization: VLMonetization? = null
    private var navType: String?= null
    private var contentPlans: List<ContentDatum?>? = null


    init {
        navType = savedStateHandle.get<String>("NAVIGATION_TYPE")
        val typeToken = object : TypeToken<List<ContentDatum>>() {}.type
        savedStateHandle.get<String>("CONTENT_TYPE").takeUnless { it == "{CONTENT_TYPE}" }?.let {
            contentPlans = Gson().fromJson(it, typeToken)
        }

        eventTrackingSDK.screenViewEvent(AnalyticsEventsKey.EVENT_VIEW_PLANS, application)
        viewModelScope.launch {
            getBootstrapData()
            apiBaseUrl = bootstrapData?.appcmsMain?.apiBaseUrl
            site = bootstrapData?.appcmsMain?.internalName.toString()
            userId = appDataRepository.getUserIdString()
            userName = appDataRepository.getUserName()
            email = appDataRepository.getEmailId() ?: appDataRepository.getUnVerifiedEmail()
            phoneNumber =
                appDataRepository.getPhoneNumber() ?: appDataRepository.getUnVerifiedPhone()
            isUserSubscribed = appDataRepository.isUserSubscribedValue()
            authToken = appDataRepository.getAuth()
            getPlanById("svodAll", null, bootstrapData?.countryCode)
        }
    }

    fun getNavType(): ViewPlanNavType? {
        return navType?.let { ViewPlanNavType.valueOf(it) }
    }

    suspend fun getBootstrapData(): BootStrapQuery.Bootstrap {
        if (bootstrapData == null) {
            bootstrapData = bootstrapRepository.getCachedBootstrap()
        }
        return bootstrapData as BootStrapQuery.Bootstrap
    }


    override fun mapIntents(intent: ViewPlanIntent): Flow<PartialState> {
        return emptyFlow()
    }

    override fun reduceUiState(
        previousState: ViewPlanUiState,
        partialState: PartialState
    ): ViewPlanUiState = when (partialState) {
        is Loading -> previousState.copy(
            isLoading = true,
        )
    }

    fun onOkPressed() {
        if (dialogState.value == BillingClient.BillingResponseCode.OK ||
            dialogState.value == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED
        ) {
            //terminate connection when switching from view plan page
            vlMonetization?.terminateConnection()
            var navDestination: String? = null
            if (phoneNumber.isNullOrEmpty() || userName.isNullOrEmpty() || email.isNullOrEmpty()) {
                navDestination = NavigationDestination.Page.route.replace(
                    oldValue = "{KEY_PAGE_PATH}",
                    newValue = NavMap.pageMap[2].replace("/", "*")
                )
                navigate(navDestination)
            } else {
                navDestination = NavigationDestination.Home.route
            }
            navigate(navDestination)
        }
        dialogState.value = DEFAULT_DIALOG_STATE
    }

    fun getImageMetaData(metadataMap: Map<String, String>): List<Image> {
        var imageList = metadataMap["imageList"]
        val typeToken = object : TypeToken<List<Image>>() {}.type
        return Gson().fromJson(imageList, typeToken)
    }

    fun navigateToHome() {
        //terminate connection when switching to view plan page
        vlMonetization?.terminateConnection()
        var navDestination: String? = null
        if (phoneNumber.isNullOrEmpty() || userName.isNullOrEmpty() || email.isNullOrEmpty()) {
            navDestination = NavigationDestination.Page.route.replace(
                oldValue = "{KEY_PAGE_PATH}",
                newValue = NavMap.pageMap[2].replace("/", "*")
            )
            navigate(navDestination)
        } else {
            navDestination = NavigationDestination.Home.route
        }
        navigate(navDestination)

    }

    fun popBackStack(){
        //terminate connection when switching to view plan page
        vlMonetization?.terminateConnection()
        navigationManager.navigate(object : NavigationCommand {
            override val destination = NavigationDestination.PopBackStack.route
        })
    }

    private fun navigate(navDestination: String) {
        navigationManager.navigate(object : NavigationCommand {
            override val destination = navDestination
            override val configuration: NavOptions = NavOptions.Builder().setPopUpTo(
                NavigationDestination.Page.route,
                inclusive = true,
                saveState = false
            ).build()
        })
    }

    private fun getPlanById(planId: String, visiblePlan: Boolean?, storeCountryCode: String?) {
        val queryData = QueryData(
            site = site,
            device = DEVICE,
            storeCountryCode = storeCountryCode,
            visible = visiblePlan,
            siteId = BuildConfig.SITE_ID
        )
        userId?.takeIf { it.isNotEmpty() }?.let {
            if (!planId.contains("svodAll")) {
                queryData.ids = planId
                queryData.userId = userId
            } else {
                queryData.userId = userId
                queryData.monetizationModel = MONETIZATION_MODEL_SVOD
            }
        } ?: kotlin.run {
            if (!planId.contains("svodAll")) {
                queryData.ids = planId
            } else {
                queryData.monetizationModel = MONETIZATION_MODEL_SVOD
            }
        }

        vlMonetization = VLMonetization(
            context = application,
            queryData = queryData,
            type = BillingClient.ProductType.SUBS,
            accessToken = authToken.toString(),
            xAPIKey = BuildConfig.X_API_KEY,
            billingListener = object : BillingListener {
                override fun onLoading() {
                    isLoading.value = true
                    Timber.d("OnLoading VLMonetization")
                }

                override fun onError(type: BillingListener.ApiType, responseCode: Int) {
                    Timber.d("OnLoading onError")

                    if (type == BillingListener.ApiType.SUBSCRIBE) {
                        dialogState.value = BillingClient.BillingResponseCode.ERROR
                    }else
                        dialogState.value = responseCode
                    isLoading.value = false
                    // Failure Eveng
                    sendPaymentFailureBeaconMessage(responseCode.toString())
                }

                override fun onPaymentInitiate(purchase: Purchase?) {
                    sendPaymentInitiateBeaconMessage(purchase)
                }

                override fun onPaymentPending(purchase: Purchase?) {
                    sendPaymentPendingBeaconMessage(purchase)
                }

                override fun onProductDetailsReceived(
                    productDetailsList: List<PlanResponse>,
                    responseCode: Int
                ) {
                    Timber.d("OnLoading onProductDetailsReceived")
                    isLoading.value = false
                    _billingConnectionState.postValue(true)

                    Timber.i("onProductDetailsReceived : ${productDetailsList.size}")

                    this@ViewPlanViewModel.filteredPlans.value = contentPlans?.let { plans ->
                        productDetailsList.filter { list -> plans.any { it?.identifier == list.identifier } }
                    } ?: productDetailsList
                }

                override fun onUpdatePurchaseState(
                    responseCode: Int,
                    purchase: Purchase?,
                    tvodPurchaseResponse: TvodPurchaseResponse?
                ) {
                    isLoading.value = false
                    if (responseCode == BillingClient.BillingResponseCode.OK) {
                        purchase?.let {
                            savePurchaseData()
                            sendAnalyticsCreateSubscription(it)
                        }
                        sendBeaconMessage()

                    }
                    dialogState.value = responseCode
                }

            }
        )

    }


    fun restorePurchase() {
        isLoading.value = true
    }

    fun launchPurchaseFlow(
        activity: Activity,
        plan: PlanResponse,
        tag: String,
    ) {
        // Add Subscribe Now
        sendSubscribeNowBeacon()
        sendAnalyticsPurchaseEvent(1)
        planToPurchase = plan
        plan.identifier?.let {
            vlMonetization?.launchBillingFlow(
                productId = it,
                activity = activity,
                tag = tag
            )
        }
    }

    private fun savePurchaseData() {
        Timber.e("savePurchaseData: --- called")
        viewModelScope.launch {
            getUserProfile()
            appDataRepository.setIsUserSubscribed(true)
        }
    }

    private fun getUserProfile() {
        viewModelScope.launch {
            userProfileUseCase(bootstrapRepository.getCachedMain()?.internalName ?: "").collect()
        }
    }


    fun setPageId(pageID: String?) {
        this.pageId = pageID
    }

    private fun sendBeaconMessage() {
        viewModelScope.launch {
            var pagePath = ""
            savedStateHandle.get<String>("KEY_PAGE_PATH")?.let {
                pagePath = it.substring(1, it.length - 1)
            }
            eventBus.invokeEvent(
                EventData(
                    eventType = BeaconEventType.PURCHASE, pageId = pageId.toString(),
                    pagePath = pagePath
                )
            )
        }
    }

    private fun sendAnalyticsCreateSubscription(purchase: Purchase) {
        val planId = planToPurchase?.id ?: ""
        val planName = planToPurchase?.planDetails?.get(0)?.title ?: ""
        val currency = planToPurchase?.planDetails?.get(0)?.countryCode ?: ""
        val transactionAmount: Double =
            (planToPurchase?.planDetails?.get(0)?.recurringPaymentAmount ?: 0).toDouble()
        val transId = purchase.orderId
        val subscriptionStartDate =
            planToPurchase?.planDetails?.get(0)?.scheduledFromDate?.toString()
        val subscriptionEndDate = planToPurchase?.planDetails?.get(0)?.scheduledToDate?.toString()
        eventTrackingSDK.sendEventSubscriptionCompleted(
            planId,
            planName,
            currency,
            transactionAmount,
            transId,
            "InApp",
            subscriptionStartDate,
            subscriptionEndDate,
            application
        )

        // Payment Success
        sendPaymentSuccessBeaconMessage(transId)

    }
    fun sendSubscribeNowBeacon() {
        viewModelScope.launch {
            // Event Beacon
            eventTrackingSDK.sendBeaconEvent(BeaconEventType.SUBSCRIBE_NOW.eventType, getBeaconRequestData(), JsonObject())
        }
    }

    fun sendPaymentInitiateBeaconMessage(purchase: Purchase?) {
        viewModelScope.launch {
            // Event Beacon

            val eventData = getPurchaseData(purchase)

            eventTrackingSDK.sendBeaconEvent(BeaconEventType.PAYMENT_INITIATE.eventType, getBeaconRequestData(), eventData)
        }
    }

    fun sendPaymentPendingBeaconMessage(purchase: Purchase?) {
        viewModelScope.launch {
            // Event Beacon

            val eventData = getPurchaseData(purchase)

            eventTrackingSDK.sendBeaconEvent(BeaconEventType.PAYMENT_PENDING.eventType, getBeaconRequestData(), eventData)
        }
    }


    private fun getPurchaseData(purchase: Purchase?): JsonObject {
        val orderAmount = (planToPurchase?.planDetails?.getOrNull(0)?.recurringPaymentAmount ?: 0).toDouble()
        val currencyCode = planToPurchase?.planDetails?.getOrNull(0)?.countryCode ?: ""
        val planDescription = planToPurchase?.planDetails?.getOrNull(0)?.description ?: ""

        val eventData = JsonObject()
        eventData.addProperty("planId", planToPurchase?.id ?: "")
        eventData.addProperty("planName", planToPurchase?.planDetails?.getOrNull(0)?.title ?: "")
        eventData.addProperty("planDesc", planDescription)
        eventData.addProperty("planType", planToPurchase?.planDetails?.getOrNull(0)?.title ?: "")
        eventData.addProperty("paymentMethod", "InApp")
        eventData.addProperty("promotionCode", "")
        eventData.addProperty("discountAmount", "")
        eventData.addProperty("purchaseType", "")
        eventData.addProperty("orderSubTotalAmount", "")
        eventData.addProperty("orderTaxAmount", "")
        eventData.addProperty("orderTotalAmount", orderAmount)
        eventData.addProperty("currencyCode", currencyCode)
        eventData.addProperty("transactionId", purchase?.orderId ?: "")
        return eventData
    }

    fun sendPaymentFailureBeaconMessage(ptransactionId: String?) {
        viewModelScope.launch {
            // Event Beacon
            val eventData = JsonObject()
            eventData.addProperty("0p/ptransactionId", ptransactionId)

            eventTrackingSDK.sendBeaconEvent(BeaconEventType.PAYMENT_FAILURE.eventType, getBeaconRequestData(), eventData)
        }
    }

    fun sendPaymentSuccessBeaconMessage(transactionId: String?) {
        viewModelScope.launch {
            // Event Beacon
            val eventData = JsonObject()
            eventData.addProperty("transactionId", transactionId)

            eventTrackingSDK.sendBeaconEvent(BeaconEventType.PAYMENT_SUCCESS.eventType, getBeaconRequestData(), eventData)
        }
    }


    fun getBeaconRequestData() : BeaconRequest? {

        var beaconRequest: BeaconRequest? = null
        runBlocking {
            val bootstrap = bootstrapRepository.getCachedBootstrap()
            val deviceId: String? = Utils.getDeviceId(application)
            val calendar = Calendar.getInstance()
            val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            val currentTimeStamp = simpleDateFormat.format(calendar.time).toString()

            beaconRequest = BeaconRequest(
                uid = appDataRepository.getUserIdString(), // User ID
                profid = appDataRepository.getProfileId(), // User ProfileID
                siteid = BuildConfig.SITE_ID, // Site ID
                pfm = "Android", // Platform
                etstamp = currentTimeStamp,  // timestamp [YYYY-MM-DD HH24:MI:SS]
                deviceid = deviceId,  // deviceId
                environment = getEnvironmentName(bootstrap?.appcmsMain?.internalName),  // Environment Name [‘Dev’, ‘Qa’, ‘Uat’, ‘Prod’]
                appversion = BuildConfig.APP_CMS_APP_VERSION  // App Version
            )
        }
        return beaconRequest
    }

    private fun sendAnalyticsPurchaseEvent(purchaseState: Int) {
        val planId = planToPurchase?.id ?: ""
        val planName = planToPurchase?.planDetails?.get(0)?.title ?: ""
        val currency = planToPurchase?.planDetails?.get(0)?.countryCode ?: ""
        val planPrice: Double =
            (planToPurchase?.planDetails?.get(0)?.recurringPaymentAmount ?: 0).toDouble()
        val contentId = appDataRepository.getActiveSubscriptionId().toString()
        val contentName = appDataRepository.getExistingSubscriptionPlanName().toString()
        val purchaseType = appDataRepository.getUserSubscriptionPlanTitle().toString()
        if (purchaseState == 1) {
            eventTrackingSDK.ecommPurchaseTvodEvent(
                planId,
                planName,
                currency,
                planPrice,
                contentId,
                contentName,
                purchaseType,
                application
            )
        } else if (purchaseState == 2) {
            eventTrackingSDK.ecommPurchaseTvodEvent(
                planId,
                planName,
                currency,
                planPrice,
                contentId,
                contentName,
                purchaseType,
                application
            )
        }
    }

    fun sendPlanSelectionBeacon(planResponse: PlanResponse?) {
        viewModelScope.launch {
            // Event Beacon
            val eventData = JsonObject()
            eventData.addProperty("planId", planResponse?.id ?: "")
            eventData.addProperty("planName",  planResponse?.planDetails?.getOrNull(0)?.title ?: "")
            eventData.addProperty("planDesc", planResponse?.planDetails?.getOrNull(0)?.description ?: "")
            eventData.addProperty("planType", planResponse?.planDetails?.getOrNull(0)?.title ?: "")

            eventTrackingSDK.sendBeaconEvent(BeaconEventType.PLAN_SELECTION.eventType, getBeaconRequestData(), eventData)
        }
    }

    companion object {
        const val DEFAULT_DIALOG_STATE = -4
    }
}