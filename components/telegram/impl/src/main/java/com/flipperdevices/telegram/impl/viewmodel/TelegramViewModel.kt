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
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.drinkless.td.libcore.telegram.Client
import org.drinkless.td.libcore.telegram.TdApi
import org.drinkless.td.libcore.telegram.TdApi.Chats

private val asciiRegexp = "[^A-Za-z0-9 ]".toRegex()

class TelegramViewModel(
    application: Application
) : AndroidLifecycleViewModel(application) {
    @Inject
    lateinit var serviceProvider: FlipperServiceProvider

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
            telegramState.onEach { state ->
                when (state) {
                    TelegramState.ENABLED -> onStartStreaming(serviceApiInternal)
                    TelegramState.DISABLED -> onPauseStreaming(serviceApiInternal)
                }
            }.launchIn(viewModelScope)
        }
        sendMessages()
        subscribeOnTelegramRequest()
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

    fun sendMessages() {
        serviceProvider.provideServiceApi(this) { serviceApiInternal ->
            serviceApiInternal.requestApi.notificationFlow().onEach {
                if (it.hasTgSendMsgRequest()) {
                    val request = it.tgSendMsgRequest
                    sendMessageInternal(request.id.toLong(), request.msg)
                }
            }.launchIn(viewModelScope)
        }
    }

    private fun sendMessageInternal(id: Long, msg: String) {
        getTelegramClient()?.send(
            TdApi.SendMessage(
                id,
                0 /* messageThreadId */,
                0 /* replyToMessageId */,
                null /* options */,
                null,
                TdApi.InputMessageText(
                    TdApi.FormattedText(msg, emptyArray()),
                    false,
                    false
                )
            ),
            DebugResultHandler(),
            TelegramExceptionHandler()
        )
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
        sendMessages()
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

    fun subscribeOnTelegramRequest() {
        serviceProvider.provideServiceApi(this) {
            it.requestApi.notificationFlow().onEach {
                if (it.hasTgStateRequest()) {
                    sendDialogs()
                }
            }.launchIn(viewModelScope)
        }
    }

    fun sendDialogs() {
        val client = telegramClint!!
        client.send(TdApi.GetChats(null, 3)) { dialog ->
            when (dialog.constructor) {
                TdApi.Error.CONSTRUCTOR -> {
                    info { "WTF DONT GOT DIALOGS: $dialog" }
                }
                TdApi.Chats.CONSTRUCTOR -> {
                    info { "WTF GOT DIALOGS: $dialog" }
                    val chatIds = (dialog as Chats).chatIds
                    info { "CHAT-IDS: $chatIds" }
                    var i = 0

                    viewModelScope.launch {
                        val telegramDialogs = chatIds.map { getDialog(client, it).single() }
                            .map {
                                TelegramDialog(
                                    it.id,
                                    it.title.take(9),
                                    arrayOf(
                                        TelegramMessage(
                                            (it.lastMessage?.content as? TdApi.MessageText)?.text?.text?.toAscii()
                                                ?.take(
                                                    19
                                                ) ?: "null",
                                            false
                                        )
                                    )
                                )
                            }.map { it.toProtobufData() }
                        serviceProvider.provideServiceApi(this@TelegramViewModel) { serviceApi ->
                            main {
                                tgStateResponse = telegramStateResponse {
                                    dialogs.addAll(
                                        telegramDialogs
                                    )
                                }
                            }.wrapToRequest().also { request ->
                                serviceApi.requestApi.request(request).launchIn(viewModelScope)
                            }
                        }
                    }
                }
                else -> {
                    info { "WTF DIALOGS: $dialog" }
                }
            }
        }
    }

    fun String.toAscii(): String {
        val newString = asciiRegexp.replace(this, "").replace(" ", "_")
        if (newString.isEmpty()) {
            return "Empty"
        }
        return newString
    }

    fun getDialog(client: Client, id: Long) = callbackFlow {
        client.send(TdApi.GetChat(id)) { dialog ->
            when (dialog.constructor) {
                TdApi.Error.CONSTRUCTOR -> {
                    info { "WTF DONT GOT DIALOGS: $dialog" }
                }
                TdApi.Chat.CONSTRUCTOR -> {
                    info { "WTF GOT DIALOGS: $dialog" }
                    launch {
                        send(dialog as TdApi.Chat)
                        close()
                    }
                }
                else -> {
                    info { "WTF DIALOGS: $dialog" }
                }
            }
        }
        awaitClose {
            info { "Close" }
        }
    }
}
