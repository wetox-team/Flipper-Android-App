package com.flipperdevices.telegram.impl.viewmodel

import androidx.lifecycle.viewModelScope
import com.flipperdevices.bridge.service.api.FlipperServiceApi
import com.flipperdevices.bridge.service.api.provider.FlipperServiceProvider
import com.flipperdevices.core.di.ComponentHolder
import com.flipperdevices.core.ui.LifecycleViewModel
import com.flipperdevices.telegram.impl.di.TelegramComponent
import com.flipperdevices.telegram.impl.model.TelegramState
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.drinkless.td.libcore.telegram.Client

class TelegramViewModel : LifecycleViewModel() {
    @Inject
    lateinit var serviceProvider: FlipperServiceProvider

    private var serviceApi: FlipperServiceApi? = null
    private var telegramClint: Client? = null
    private val telegramState = MutableStateFlow(TelegramState.DISABLED)
    private val msgText = MutableStateFlow("Stoped")
    private val telegramPhoneNumber = MutableStateFlow("")
    private val telegramAuthCode = MutableStateFlow("")

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
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                telegramClint = Client.create(
                    //todo: UpdateHandler(),
                    null,
                    null
                )
            }
        }
    }

    fun getMsgText() = msgText
    fun getTelegramState() = telegramState
    fun getTelegramPhoneNumber() = telegramPhoneNumber
    fun getTelegramAuthCode() = telegramAuthCode

    private fun onStartStreaming(serviceApi: FlipperServiceApi) {
        msgText.value = "Started";
    }

    private fun onPauseStreaming(serviceApi: FlipperServiceApi) {
        msgText.value = "Stoped";
    }
}
