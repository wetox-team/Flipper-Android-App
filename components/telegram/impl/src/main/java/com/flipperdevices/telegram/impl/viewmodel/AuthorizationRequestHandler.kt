package com.flipperdevices.telegram.impl.viewmodel

import com.flipperdevices.core.log.LogTagProvider
import org.drinkless.td.libcore.telegram.Client
import org.drinkless.td.libcore.telegram.TdApi

class AuthorizationRequestHandler : Client.ResultHandler, LogTagProvider {
    override val TAG = "AuthorizationRequestHandler"

    override fun onResult(tdLibObject: TdApi.Object) {
        when (tdLibObject.constructor) {
            TdApi.Error.CONSTRUCTOR -> {
                error { "Receive an error: $tdLibObject`" }
            }
            TdApi.Ok.CONSTRUCTOR -> {
            }
            else -> error { "Receive wrong response from TDLib:$tdLibObject`" }
        }
    }
}
