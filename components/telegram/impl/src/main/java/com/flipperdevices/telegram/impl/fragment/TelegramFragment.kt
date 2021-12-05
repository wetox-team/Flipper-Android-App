package com.flipperdevices.telegram.impl.fragment

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.fragment.app.viewModels
import com.flipperdevices.core.log.info
import com.flipperdevices.core.ui.ComposeFragment
import com.flipperdevices.telegram.impl.composable.ComposableScreen
import com.flipperdevices.telegram.impl.model.TelegramState
import com.flipperdevices.telegram.impl.viewmodel.TelegramViewModel

class TelegramFragment : ComposeFragment() {
    private val telegramViewModel by viewModels<TelegramViewModel>()

    @ExperimentalFoundationApi
    @ExperimentalComposeUiApi
    @Composable
    override fun RenderView() {
        ComposableScreen(
            telegramViewModel,
            onScreenStreamingSwitch = { state ->
                if (state == TelegramState.ENABLED) {
                    telegramViewModel.getTelegramState().compareAndSet(
                        expect = TelegramState.DISABLED,
                        update = TelegramState.ENABLED
                    )
                } else if (state == TelegramState.DISABLED) {
                    telegramViewModel.getTelegramState().compareAndSet(
                        expect = TelegramState.ENABLED,
                        update = TelegramState.DISABLED
                    )
                }
            },
            onPhoneFilling = { phone ->
                if (telegramViewModel.getTelegramState().value == TelegramState.ENABLED) {
                    if (phone.isEmpty()) {
                        telegramViewModel.getMsgText().value = "Error: enter phone number"
                    } else {
                        telegramViewModel.getTelegramPhoneNumber().value = phone
                        telegramViewModel.getMsgText().value = "OK, now enter auth code from TG"
                        telegramViewModel.getTelegramPhoneNumberReady().value = true
                        telegramViewModel.requestCodeTelegram()
                    }
                } else {
                    telegramViewModel.getMsgText().value = "Error: you need to start telegram"
                }
            },
            onAuthCodeFilling = { code ->
                if (telegramViewModel.getTelegramState().value == TelegramState.ENABLED) {
                    if (telegramViewModel.getTelegramPhoneNumber().value.isEmpty()) {
                        telegramViewModel.getMsgText().value = "Error: enter phone number"
                    } else if (code.isEmpty()) {
                        telegramViewModel.getMsgText().value = "Error: enter code"
                    } else {
                        telegramViewModel.getTelegramAuthCode().value = code
                        telegramViewModel.getTelegramAuthCodeReady().value = true
                        telegramViewModel.getMsgText().value = "Success!"
                    }
                } else {
                    telegramViewModel.getMsgText().value = "Error: you need to start telegram"
                }
            },
        )
    }

    override fun onPause() {
        super.onPause()
        telegramViewModel.getTelegramState().compareAndSet(
            expect = TelegramState.ENABLED,
            update = TelegramState.DISABLED
        )
    }
}
