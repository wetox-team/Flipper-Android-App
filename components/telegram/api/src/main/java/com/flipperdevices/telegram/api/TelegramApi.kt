package com.flipperdevices.telegram.api

import com.github.terrakok.cicerone.Screen

interface TelegramApi {
    fun provideScreen(): Screen
}
