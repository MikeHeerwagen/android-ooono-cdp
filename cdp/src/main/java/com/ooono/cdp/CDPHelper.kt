package com.ooono.cdp

import android.content.Context
import android.util.Log
import com.ooono.cdp.RudderStack.Companion.TAG


interface CDP {

    /// Start the CDP. It is important to start the CDP before starting to track events.
    fun start(context: Context)

    /// Use this function in the beginning to identify the user. It is not necessary to identify the user, however it makes sense in most cases.
    fun identify(traits: Map<String, Any>? = null)

    /// Track data with an eventName and the data.
    fun track(eventName: String, data: Map<String, Any?>)

    /// Track data for a certain aggregateDataKey.
    fun track(aggregateDataKey: String)

    /// Stores the data for later use. When storing data, the aggregateDataKey will be the same as the event name.
    /// - Parameters:
    ///   - aggregateDataKey: The key for where the data should be stored. All data must me stored under a key.
    ///   - attributeName: The name of the attribute. For example "startTime"
    ///   - data: The data that should be tracked.
    ///   - action: ADD_TO_EXISTING If the data is a number that can be appended to, set this value to true. For example, if numberOfReceivedPins increase by 1, you should set this value to "true" and send 1 as data.
    ///   - action: OVERWRITE If the data should overwrite existing value.
    fun aggregateData(
        aggregateDataKey: String,
        attributeName: String,
        data: Any?,
        action: CDPHelper.AdditionalAction? = null
    )

    //Aggregates a map of data points to an event
    fun aggregateData(aggregateDataKey: String, map: Map<String, Any?>)

    fun clearAggregatedData(aggregateDataKey: String)

    fun isUserIdentified():Boolean
}

abstract class CDPHelper : CDP {

    var eventMap: MutableMap<String, MutableMap<String, Any?>> = mutableMapOf()

    fun handleData(
        aggregateDataKey: String,
        attributeName: String,
        data: Any?,
        action: AdditionalAction?
    ) {
        if (checkIfEventAlreadyExist(aggregateDataKey)) {
            when (action) {
                AdditionalAction.ADD_TO_EXISTING -> {
                    incrementExistingNumber(aggregateDataKey, attributeName, data)
                }

                AdditionalAction.OVERWRITE -> {
                    replaceValue(aggregateDataKey, attributeName, data)
                }

                else -> {
                    addNewAttributeToEvent(aggregateDataKey, attributeName, data)
                }
            }
        } else {
            createNewEvent(aggregateDataKey, attributeName, data)
        }
    }

    fun handleData(aggregateDataKey: String, map: Map<String, Any?>) {
        if (checkIfEventAlreadyExist(aggregateDataKey)) {
            eventMap[aggregateDataKey]?.putAll(map)
        } else {
            val trackedValuesMap = mutableMapOf<String, Any?>()
            trackedValuesMap.putAll(map)
            eventMap[aggregateDataKey] = trackedValuesMap
        }
    }

    private fun addNewAttributeToEvent(
        aggregateDataKey: String,
        attributeName: String,
        data: Any?
    ) {
        eventMap[aggregateDataKey]?.put(attributeName, data)
    }

    private fun createNewEvent(aggregateDataKey: String, attributeName: String, data: Any?) {
        val trackedValuesMap = mutableMapOf<String, Any?>()
        trackedValuesMap[attributeName] = data
        eventMap[aggregateDataKey] = trackedValuesMap
    }

    private fun incrementExistingNumber(
        aggregateDataKey: String,
        attributeName: String,
        data: Any?
    ) {
        val trackedValuesMap = eventMap[aggregateDataKey]
        trackedValuesMap?.let {
            when (val savedNumber = it[attributeName]) {
                is Int -> {
                    trackedValuesMap[attributeName] = savedNumber + 1
                }

                is Float -> {
                    if (data is Float) {
                        trackedValuesMap[attributeName] = savedNumber + data
                    } else {
                        Log.d(TAG, "saved attribute $attributeName isn't type of float")
                    }
                }

                is Double -> {
                    if (data is Double) {
                        trackedValuesMap[attributeName] = savedNumber + data
                    } else {
                        Log.d(TAG, "saved attribute $attributeName isn't type of double")
                    }
                }

                else -> {
                    Log.d(TAG, "attribute $attributeName isn't type of double,float or integer")
                }
            }
        }
    }

    private fun replaceValue(aggregateDataKey: String, attributeName: String, data: Any?) {
        val trackedValuesMap = eventMap[aggregateDataKey]
        trackedValuesMap?.let {
            trackedValuesMap[attributeName] = data
        }
    }

    private fun checkIfEventAlreadyExist(aggregateDataKey: String): Boolean {
        return eventMap.containsKey(aggregateDataKey)
    }

    enum class AdditionalAction {
        ADD_TO_EXISTING, OVERWRITE
    }
}
