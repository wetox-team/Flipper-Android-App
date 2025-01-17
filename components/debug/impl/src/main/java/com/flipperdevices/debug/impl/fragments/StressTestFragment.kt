package com.flipperdevices.debug.impl.fragments

import androidx.compose.runtime.Composable
import androidx.fragment.app.viewModels
import com.flipperdevices.core.ui.ComposeFragment
import com.flipperdevices.debug.impl.compose.ComposableStressTestScreen
import com.flipperdevices.debug.impl.viewmodel.StressTestViewModel

class StressTestFragment : ComposeFragment() {
    private val stressTestViewModel by viewModels<StressTestViewModel>()

    @Composable
    override fun RenderView() {
        ComposableStressTestScreen(stressTestViewModel)
    }
}
