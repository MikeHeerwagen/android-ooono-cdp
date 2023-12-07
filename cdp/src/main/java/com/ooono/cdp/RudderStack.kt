package com.ooono.cdp

import android.content.Context
import android.util.Log
import com.ooono.cdp.config.Config
import com.ooono.cdp.config.RudderConfig
import com.rudderstack.android.sdk.core.RudderClient
import com.rudderstack.android.sdk.core.RudderProperty
import com.rudderstack.android.sdk.core.RudderTraits
import com.rudderstack.android.sdk.core.RudderConfig as RudderStackConfig

class RudderStack(private val config: Config) : CDPHelper() {

    internal lateinit var rudderClient: RudderClient

    override fun start(context: Context) {
        if (config !is RudderConfig) throw IllegalArgumentException("Wrong config used - Use ${RudderConfig::class.java.name}")
        if (!config.isDebug) {
            rudderClient = RudderClient.getInstance(
                context,
                config.writeKey,
                RudderStackConfig.Builder()
                    .withDataPlaneUrl(config.url)
                    .withTrackLifecycleEvents(false)
                    .build()
            )
            rudderClient.startSession()
        }
    }

    override fun identify(traits: Map<String, Any>?) {
        if (!isRudderClientInitialized()) return
        traits ?: return
        val rudderTraits = RudderTraits()
        traits.forEach { (key, data) ->
            rudderTraits.put(key, data)
        }
        rudderClient.identify(rudderTraits)
    }

    override fun track(eventName: String, data: Map<String, Any?>) {
        if (!isRudderClientInitialized()){
            Log.d(TAG, "qqq tracking even $eventName with properties $data")
            return
        }
        rudderClient.track(eventName, RudderProperty().putValue(data))
    }

    override fun track(aggregateDataKey: String) {
        if (!isRudderClientInitialized()) return
        eventMap[aggregateDataKey]?.let {
            rudderClient.track(
                aggregateDataKey,
                RudderProperty().putValue(eventMap[aggregateDataKey])
            )
            clearAggregatedData(aggregateDataKey)
        }
    }

    override fun aggregateData(
        aggregateDataKey: String,
        attributeName: String,
        data: Any?,
        action: AdditionalAction?
    ) {
        if (!isRudderClientInitialized()) return
        handleData(aggregateDataKey, attributeName, data, action)
    }
    override fun aggregateData(
        aggregateDataKey: String,
        map: Map<String,Any?>
    ) {
        if (!isRudderClientInitialized()) return
        handleData(aggregateDataKey, map)
    }

    override fun clearAggregatedData(aggregateDataKey: String) {
        eventMap.remove(aggregateDataKey)
    }

    override fun isUserIdentified(): Boolean {
        if(!isRudderClientInitialized()) return false
        return rudderClient.rudderContext?.traits?.get(USER_ID_KEY) !=null
    }

    private fun isRudderClientInitialized(): Boolean =
        ::rudderClient.isInitialized

    companion object {
        val TAG: String = RudderStack::class.java.name
        private const val USER_ID_KEY = "userId"
    }
}
