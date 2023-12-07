package com.ooono.cdp.config

data class RudderConfig(
    override val url: String,
    override val writeKey: String,
    override val isDebug: Boolean
): Config
