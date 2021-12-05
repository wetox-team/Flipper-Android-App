package com.flipperdevices.telegram.impl.viewmodel

import com.flipperdevices.core.log.LogTagProvider
import com.flipperdevices.core.log.info
import java.io.File
import org.drinkless.td.libcore.telegram.Client
import org.drinkless.td.libcore.telegram.TdApi
import org.drinkless.td.libcore.telegram.TdApi.CheckDatabaseEncryptionKey
import org.drinkless.td.libcore.telegram.TdApi.SetAuthenticationPhoneNumber

class TelegramDebugResultHandler(
    private val phoneNumber: String,
    private val databaseDir: File
) : Client.ResultHandler, LogTagProvider {
    override val TAG = "TelegramDebugResultHandler"
    private lateinit var client: Client

    override fun onResult(tdLibObject: TdApi.Object) {
        info { "Receive $tdLibObject" }

        val authorizationState = tdLibObject as? TdApi.UpdateAuthorizationState ?: return

        when (authorizationState.authorizationState.constructor) {
            TdApi.AuthorizationStateWaitTdlibParameters.CONSTRUCTOR -> {
                val parameters = TdApi.TdlibParameters()
                parameters.databaseDirectory = databaseDir.absolutePath
                parameters.useMessageDatabase = true
                parameters.useSecretChats = false
                parameters.apiId = 962214
                parameters.apiHash = "398276370c4ba2b3b9f6ae27410c817e"
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
                client.send(
                    SetAuthenticationPhoneNumber(phoneNumber, null),
                    AuthorizationRequestHandler()
                )
            }
        }
    }

    fun inject(client: Client) {
        this.client = client
    }
}
