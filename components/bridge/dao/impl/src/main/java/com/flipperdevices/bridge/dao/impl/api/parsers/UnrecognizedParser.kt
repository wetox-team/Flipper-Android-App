package com.flipperdevices.bridge.dao.impl.api.parsers

import com.flipperdevices.bridge.dao.api.model.FlipperKey
import com.flipperdevices.bridge.dao.api.model.parsed.FlipperKeyParsed

class UnrecognizedParser : KeyParserDelegate {
    override suspend fun parseKey(
        flipperKey: FlipperKey,
        keyContentAsPairs: List<Pair<String, String>>
    ): FlipperKeyParsed {
        return FlipperKeyParsed.Unrecognized(
            flipperKey.path.name,
            flipperKey.path.fileType
        )
    }
}
