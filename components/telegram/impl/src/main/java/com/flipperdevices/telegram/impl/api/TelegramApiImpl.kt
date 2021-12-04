package com.flipperdevices.telegram.impl.api

import com.flipperdevices.core.di.AppGraph
import com.flipperdevices.telegram.api.TelegramApi
import com.flipperdevices.telegram.impl.fragment.TelegramFragment
import com.github.terrakok.cicerone.Screen
import com.github.terrakok.cicerone.androidx.FragmentScreen
import com.squareup.anvil.annotations.ContributesBinding
import javax.inject.Inject

@ContributesBinding(AppGraph::class)
class TelegramApiImpl @Inject constructor() : TelegramApi {
    override fun provideScreen(): Screen {
        return FragmentScreen { TelegramFragment() }
    }
}
