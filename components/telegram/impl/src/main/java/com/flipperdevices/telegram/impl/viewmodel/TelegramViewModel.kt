package com.flipperdevices.telegram.impl.viewmodel

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.flipperdevices.bridge.api.model.wrapToRequest
import com.flipperdevices.bridge.service.api.FlipperServiceApi
import com.flipperdevices.bridge.service.api.provider.FlipperServiceProvider
import com.flipperdevices.core.di.ComponentHolder
import com.flipperdevices.core.log.info
import com.flipperdevices.core.ui.AndroidLifecycleViewModel
import com.flipperdevices.protobuf.main
import com.flipperdevices.protobuf.telegram.telegramDialog
import com.flipperdevices.protobuf.telegram.telegramMessage
import com.flipperdevices.protobuf.telegram.telegramStateResponse
import com.flipperdevices.telegram.impl.di.TelegramComponent
import com.flipperdevices.telegram.impl.model.TelegramDialog
import com.flipperdevices.telegram.impl.model.TelegramMessage
import com.flipperdevices.telegram.impl.model.TelegramState
import java.io.File
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.drinkless.td.libcore.telegram.Client
import org.drinkless.td.libcore.telegram.TdApi
import org.drinkless.td.libcore.telegram.TdApi.ChatListMain
import org.drinkless.td.libcore.telegram.TdApi.Chats
import org.drinkless.td.libcore.telegram.TdApi.LoadChats


class TelegramViewModel(
    application: Application
) : AndroidLifecycleViewModel(application) {
    @Inject
    lateinit var serviceProvider: FlipperServiceProvider

    private var serviceApi: FlipperServiceApi? = null
    private var telegramClint: Client? = null
    private val telegramState = MutableStateFlow(TelegramState.DISABLED)
    private val msgText = MutableStateFlow("Stoped")
    private val telegramPhoneNumberReady = MutableStateFlow<String?>(null)
    private val telegramAuthCodeReady = MutableStateFlow<String?>(null)
    private val tdLibDatabase = File(application.filesDir, "tdlibdatabase")
    private val telegramDialogsState: Array<TelegramDialog?> = arrayOf(null, null, null)

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
        var view = this
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val resultHandler = TelegramDebugResultHandler(tdLibDatabase, viewModelScope)
                telegramClint = Client.create(
                    resultHandler,
                    TelegramExceptionHandler(),
                    TelegramExceptionHandler()
                )
                resultHandler.inject(view)
                telegramPhoneNumberReady.emit(phone)
            }
        }
    }

    fun onSmsCode(code: String) {
        viewModelScope.launch {
            telegramAuthCodeReady.emit(code)
        }
    }

    fun getMsgText() = msgText
    fun getTelegramState() = telegramState
    fun getTelegramDialogsState() = telegramDialogsState
    fun getTelegramClient() = telegramClint
    fun getTelegramPhoneNumberReady(): StateFlow<String?> = telegramPhoneNumberReady
    fun getTelegramAuthCodeReady(): StateFlow<String?> = telegramAuthCodeReady

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

    fun send2flipper() {
        serviceProvider.provideServiceApi(this) {
            it.requestApi.request(
                main {
                    tgStateResponse = telegramStateResponse {
                        dialogs.add(
                            telegramDialog {
                                id = 1
                                name = "Test"
                                messages.add(
                                    telegramMessage {
                                        text = "Test Message"
                                        isOur = true
                                    }
                                )
                            }
                        )
                    }
                }.wrapToRequest()
            ).launchIn(viewModelScope)
        }
    }

    /*fun getDialogs() {
        telegramClint!!.send(
            LoadChats(ChatListMain(), 3)
        ) { dialog ->
            when (dialog.constructor) {
                TdApi.Error.CONSTRUCTOR -> {info {"WTF DONT GOT DIALOGS: $dialog"}}
                TdApi.Ok.CONSTRUCTOR -> {
                    info {"WTF GOT DIALOGS: $dialog"}
                    val chatIds = (dialog as Chats).chatIds
                    info { "CHAT-IDS: $chatIds" }
                    var i = 0;
                    chatIds.forEach { id ->
                        telegramDialogsState[i] = TelegramDialog(
                            id=id,
                            name=null,
                            messages = arrayOf()
                        )
                        i++
                    }
                }
                else -> {info {"WTF DIALOGS: $dialog"}}
            }
        }
    }*/

    fun getDialogs() {
        telegramClint!!.send(TdApi.GetChats(null, 3)) { dialog ->
            when (dialog.constructor) {
                TdApi.Error.CONSTRUCTOR -> {info {"WTF DONT GOT DIALOGS: $dialog"}}
                TdApi.Chats.CONSTRUCTOR -> {
                    info {"WTF GOT DIALOGS: $dialog"}
                    val chatIds = (dialog as Chats).chatIds
                    info { "CHAT-IDS: $chatIds" }
                    var i = 0;
                    chatIds.forEach { id ->
                        telegramDialogsState[i] = TelegramDialog(
                            id=id,
                            name=null,
                            messages = arrayOf()
                        )
                        i++
                    }
                    var a = telegramDialogsState;
                }
                else -> {info {"WTF DIALOGS: $dialog"}}
            }
        }
}}
