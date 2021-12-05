package com.flipperdevices.telegram.impl.viewmodel

import com.flipperdevices.core.log.LogTagProvider
import com.flipperdevices.core.log.info
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.drinkless.td.libcore.telegram.Client
import org.drinkless.td.libcore.telegram.TdApi
import org.drinkless.td.libcore.telegram.TdApi.CheckDatabaseEncryptionKey
import org.drinkless.td.libcore.telegram.TdApi.SetAuthenticationPhoneNumber

class TelegramDebugResultHandler(
    private val databaseDir: File,
    private val scope: CoroutineScope
) : Client.ResultHandler, LogTagProvider {
    override val TAG = "TelegramDebugResultHandler"
    private lateinit var tgViewModel: TelegramViewModel
    private lateinit var client: Client

    override fun onResult(tdLibObject: TdApi.Object) {
        // info { "Receive $tdLibObject" }

        val authorizationState = tdLibObject as? TdApi.UpdateAuthorizationState ?: return

        when (authorizationState.authorizationState.constructor) {
            TdApi.AuthorizationStateWaitTdlibParameters.CONSTRUCTOR -> {
                val parameters = TdApi.TdlibParameters()
                parameters.databaseDirectory = databaseDir.absolutePath
                parameters.useMessageDatabase = true
                parameters.useSecretChats = false
                parameters.apiId = 19653082
                parameters.apiHash = "8f546f769a4c769cf75ae8b868dad4ad"
                parameters.systemLanguageCode = "en"
                parameters.deviceModel = "Desktop"
                parameters.applicationVersion = "1.0"
                parameters.enableStorageOptimizer = true

                client.send(
                    TdApi.SetTdlibParameters(parameters),
                    AuthorizationRequestHandler()
                )
            }
            TdApi.AuthorizationStateWaitEncryptionKey.CONSTRUCTOR -> {
                client.send(CheckDatabaseEncryptionKey(), AuthorizationRequestHandler())
            }
            TdApi.AuthorizationStateWaitPhoneNumber.CONSTRUCTOR -> {
                scope.launch {
                    tgViewModel.getTelegramPhoneNumberReady().collect { phoneNumber ->
                        if (phoneNumber == null) {
                            return@collect
                        }
                        info { "WTF-PHONE-READY: $phoneNumber" }
                        client.send(
                            SetAuthenticationPhoneNumber(
                                phoneNumber,
                                null
                            ),
                            AuthorizationRequestHandler()
                        )
                    }
                }
            }
            TdApi.AuthorizationStateWaitCode.CONSTRUCTOR -> {
                scope.launch {
                    tgViewModel.getTelegramAuthCodeReady().collect { authCode ->
                        if (authCode == null) {
                            return@collect
                        }
                        client.send(
                            TdApi.CheckAuthenticationCode(authCode),
                            AuthorizationRequestHandler()
                        )
                    }
                }
            }
        }
    }

    fun inject(tgViewModel: TelegramViewModel) {
        this.tgViewModel = tgViewModel
        this.client = tgViewModel.getTelegramClient()!!
    }
}
