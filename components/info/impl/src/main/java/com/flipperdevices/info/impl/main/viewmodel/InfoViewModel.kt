package com.flipperdevices.info.impl.main.viewmodel

import androidx.lifecycle.viewModelScope
import com.ezhevita.protobuf.telegram.telegramDialog
import com.ezhevita.protobuf.telegram.telegramMessage
import com.ezhevita.protobuf.telegram.telegramStateResponse
import com.flipperdevices.bridge.api.model.FlipperGATTInformation
import com.flipperdevices.bridge.api.model.wrapToRequest
import com.flipperdevices.bridge.service.api.FlipperServiceApi
import com.flipperdevices.bridge.service.api.provider.FlipperBleServiceConsumer
import com.flipperdevices.bridge.service.api.provider.FlipperServiceProvider
import com.flipperdevices.core.di.ComponentHolder
import com.flipperdevices.core.ui.LifecycleViewModel
import com.flipperdevices.info.impl.di.InfoComponent
import com.flipperdevices.protobuf.main
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import no.nordicsemi.android.ble.ktx.state.ConnectionState

class InfoViewModel : LifecycleViewModel(), FlipperBleServiceConsumer {
    @Inject
    lateinit var bleService: FlipperServiceProvider

    private val informationState = MutableStateFlow(FlipperGATTInformation())
    private val connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnecting)

    init {
        ComponentHolder.component<InfoComponent>().inject(this)
        bleService.provideServiceApi(consumer = this, lifecycleOwner = this)
    }

    fun getDeviceInformation(): StateFlow<FlipperGATTInformation> {
        return informationState
    }

    fun getConnectionState(): StateFlow<ConnectionState> {
        return connectionState
    }

    fun sendTestCommand() {
        bleService.provideServiceApi(this) {
            it.requestApi.request(
                main {
                    telegramStateResponse {
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

    override fun onServiceApiReady(serviceApi: FlipperServiceApi) {
        serviceApi.flipperInformationApi.getInformationFlow().onEach {
            informationState.emit(it)
        }.launchIn(viewModelScope)
        serviceApi.connectionInformationApi.getConnectionStateFlow().onEach {
            connectionState.emit(it)
        }.launchIn(viewModelScope)
    }
}
