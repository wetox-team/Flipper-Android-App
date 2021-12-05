package com.flipperdevices.telegram.impl.viewmodel

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.flipperdevices.bridge.service.api.FlipperServiceApi
import com.flipperdevices.bridge.service.api.provider.FlipperServiceProvider
import com.flipperdevices.core.di.ComponentHolder
import com.flipperdevices.core.ui.AndroidLifecycleViewModel
import com.flipperdevices.telegram.impl.di.TelegramComponent
import com.flipperdevices.telegram.impl.model.TelegramDialog
import com.flipperdevices.telegram.impl.model.TelegramMessage
import com.flipperdevices.telegram.impl.model.TelegramState
import java.io.File
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.drinkless.td.libcore.telegram.Client

class TelegramViewModel(
    application: Application
) : AndroidLifecycleViewModel(application) {
    @Inject
    lateinit var serviceProvider: FlipperServiceProvider

    private var serviceApi: FlipperServiceApi? = null
    private var telegramClint: Client? = null
    private val telegramState = MutableStateFlow(TelegramState.DISABLED)
    private val msgText = MutableStateFlow("Stoped")
    private val telegramPhoneNumber = MutableStateFlow("")
    private val telegramAuthCode = MutableStateFlow("")
    private val tdLibDatabase = File(application.filesDir, "tdlibdatabase")
    private val telegramDialogsState: Array<TelegramDialog?> = arrayOf(null, null, null)
    private val telegramMessagesState: Array<TelegramMessage?> = arrayOf(null, null, null)

    init {
        ComponentHolder.component<TelegramComponent>().inject(this)
        serviceProvider.provideServiceApi(this) { serviceApiInternal ->
            serviceApi = serviceApiInternal
            telegramState.onEach { state ->
                when (state) {
                    TelegramState.ENABLED -> onStartStreaming(serviceApiInternal)
                    TelegramState.DISABLED -> onPauseStreaming(serviceApiInternal)
                }
            }.launchIn(viewModelScope)
        }
    }

    fun requestCodeTelegram(phone: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val resultHandler = TelegramDebugResultHandler(phone, tdLibDatabase)
                telegramClint = Client.create(
                    resultHandler,
                    TelegramExceptionHandler(),
                    TelegramExceptionHandler()
                )
                resultHandler.inject(telegramClint!!)
            }
        }
    }

    fun getMsgText() = msgText
    fun getTelegramState() = telegramState
    fun getTelegramPhoneNumber() = telegramPhoneNumber
    fun getTelegramAuthCode() = telegramAuthCode
    fun getTelegramDialogsState() = telegramDialogsState
    fun getTelegramMessagesState() = telegramMessagesState

    private fun onStartStreaming(serviceApi: FlipperServiceApi) {
        msgText.value = "Started"
    }

    private fun onPauseStreaming(serviceApi: FlipperServiceApi) {
        msgText.value = "Stoped"
    }

    fun loadLast3Chats() {
        // todo: get last 3 chats from tdlib and update this.telegramDialogsState
    }

    fun loadLast3Messages() {
        // todo: get last 3 messages from tdlib and update this.telegramMessagesState
    }
}
