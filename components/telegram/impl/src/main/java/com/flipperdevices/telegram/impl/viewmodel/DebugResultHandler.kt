package com.flipperdevices.telegram.impl.viewmodel

import com.flipperdevices.core.log.LogTagProvider
import com.flipperdevices.core.log.info
import org.drinkless.td.libcore.telegram.Client
import org.drinkless.td.libcore.telegram.TdApi

class DebugResultHandler : Client.ResultHandler, LogTagProvider {
    override val TAG = "DebugResultHandler"

    override fun onResult(tdApi: TdApi.Object?) {
        info { "Send $tdApi" }
    }
}
