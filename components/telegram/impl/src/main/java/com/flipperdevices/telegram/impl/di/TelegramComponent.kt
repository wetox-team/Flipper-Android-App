package com.flipperdevices.telegram.impl.di

import com.flipperdevices.core.di.AppGraph
import com.flipperdevices.telegram.impl.viewmodel.TelegramViewModel
import com.squareup.anvil.annotations.ContributesTo

@ContributesTo(AppGraph::class)
interface TelegramComponent {
    fun inject(viewModel: TelegramViewModel)
}
