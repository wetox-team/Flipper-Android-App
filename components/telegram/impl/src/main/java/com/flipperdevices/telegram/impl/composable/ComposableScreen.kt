package com.flipperdevices.telegram.impl.composable

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.flipperdevices.telegram.impl.R
import com.flipperdevices.telegram.impl.model.TelegramState
import com.flipperdevices.telegram.impl.viewmodel.TelegramViewModel

@ExperimentalFoundationApi
@ExperimentalComposeUiApi
@Preview(
    showBackground = true,
    showSystemUi = true
)
@Composable
fun ComposableScreen(
    viewModel: TelegramViewModel = viewModel(),
    onScreenStreamingSwitch: (TelegramState) -> Unit = {},
    onPhoneFilling: (String) -> Unit = {},
    onAuthCodeFilling: (String) -> Unit = {}
) {
    val msgText by viewModel.getMsgText().collectAsState()
    val telegramState by viewModel.getTelegramState().collectAsState()

    Column() {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(all = 16.dp)
                .weight(weight = 1f),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = msgText,
                modifier = Modifier.padding(top = 10.dp),
            )

            var telegramPhoneNumber by remember { mutableStateOf("") }
            var telegramCode by remember { mutableStateOf("") }

            TextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp),
                value = telegramPhoneNumber,
                onValueChange = {
                    if (it.endsWith("\n"))
                        onPhoneFilling(it.removeSuffix("\n"))
                    else
                        telegramPhoneNumber = it
                },
                label = { Text("phone number") },

            )
            TextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 200.dp),
                value = telegramCode,
                onValueChange = {
                    if (it.endsWith("\n"))
                        onAuthCodeFilling(it.removeSuffix("\n"))
                    else
                        telegramCode = it
                },
                label = { Text("code") }
            )

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    onScreenStreamingSwitch(
                        when (telegramState) {
                            TelegramState.DISABLED -> TelegramState.ENABLED
                            TelegramState.ENABLED -> TelegramState.DISABLED
                        }
                    )
                }
            ) {
                Text(
                    text = stringResource(
                        id = when (telegramState) {
                            TelegramState.DISABLED -> R.string.enable_telegram
                            TelegramState.ENABLED -> R.string.disable_telegram
                        }
                    ),
                )
            }
        }
    }
}
