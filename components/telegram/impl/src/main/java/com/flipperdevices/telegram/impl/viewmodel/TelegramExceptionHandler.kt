package com.flipperdevices.telegram.impl.viewmodel

import com.flipperdevices.core.log.LogTagProvider
import com.flipperdevices.core.log.error
import org.drinkless.td.libcore.telegram.Client

class TelegramExceptionHandler : Client.ExceptionHandler, LogTagProvider {
    override val TAG = "TelegramExceptionHandler"

    override fun onException(e: Throwable?) {
        error(e) { "Error in telegram" }
    }
}
