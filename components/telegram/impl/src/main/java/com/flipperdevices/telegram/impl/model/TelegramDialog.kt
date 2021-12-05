package com.flipperdevices.telegram.impl.model

import com.flipperdevices.protobuf.telegram.Telegram
import com.flipperdevices.protobuf.telegram.telegramDialog
import com.flipperdevices.protobuf.telegram.telegramMessage

data class TelegramDialog(
    var id: Long,
    var name: String,
    var messages: Array<TelegramMessage>
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TelegramDialog

        if (id != other.id) return false
        if (name != other.name) return false
        if (!messages.contentEquals(other.messages)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + messages.contentHashCode()
        return result
    }

    fun toProtobufData(): Telegram.TelegramDialog {
        return telegramDialog {
            this.id = this@TelegramDialog.id.toInt()
            this.name = this@TelegramDialog.name
            this.messages.addAll(
                this@TelegramDialog.messages.map {
                    telegramMessage {
                        text = it.text
                        isOur = it.is_our
                    }
                }
            )
        }
    }
}
