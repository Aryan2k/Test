package com.viewlift.monetization.data.model

enum class PurchaseResultCode(val errorCode : Int ) {
    UNABLE_TO_CONNECT(-1),
    PRODUCT_DETAILS_UNAVAILABLE(0),
    PRODUCT_DETAILS_AVAILABLE(1),
    PURCHASE_DONE(2),
    PURCHASE_CANCEL(3),
    DEVELOPER_ERROR(4),
    ITEM_ALREADY_OWNED(5),
    ITEM_UNAVAILABLE(6),
    SERVICE_UNAVAILABLE(7),
    ITEM_NOT_OWNED(8)
}

data class PurchaseResult(var PurchaseResultCode : PurchaseResultCode,
                          var resultData : String?,
                          var paymentUniqueId: String?,
                          var retryCount : Int)

