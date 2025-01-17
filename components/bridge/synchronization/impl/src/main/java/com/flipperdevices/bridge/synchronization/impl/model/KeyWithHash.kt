package com.flipperdevices.bridge.synchronization.impl.model

import com.flipperdevices.bridge.dao.api.model.FlipperKeyPath
import kotlinx.serialization.Serializable

@Serializable
data class KeyWithHash(
    val keyPath: FlipperKeyPath,
    val hash: String
)
