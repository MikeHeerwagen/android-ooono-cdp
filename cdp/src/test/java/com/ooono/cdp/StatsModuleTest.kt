package com.ooono.cdp

import com.ooono.cdp.config.RudderConfig
import com.rudderstack.android.sdk.core.RudderClient
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations

/**
 * Testing aggregated part of the statsmodule
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class StatsModuleTest {

    private lateinit var rudderStack: RudderStack

    @Mock
    lateinit var rudderClient: RudderClient

    private lateinit var closeable: AutoCloseable

    @Before
    fun init() {
        closeable = MockitoAnnotations.openMocks(this)
        rudderStack = RudderStack(RudderConfig(url = URL, writeKey = WRITE_KEY, true))
        rudderStack.rudderClient = rudderClient
    }

    @Test
    fun first_run_create_aggregated_data() {
        rudderStack.aggregateData(aggregateDataKey = EVENT_1, attributeName = ATTRIBUTE_1, DATA_1)
        assertEquals(1, rudderStack.eventMap.size)
        assertEquals(1, rudderStack.eventMap[EVENT_1]?.values?.size)
    }

    @Test
    fun add_to_existing_aggregated_data() {
        rudderStack.aggregateData(aggregateDataKey = EVENT_1, attributeName = ATTRIBUTE_1, DATA_1)
        assertEquals(1, rudderStack.eventMap.size)
        assertEquals(1, rudderStack.eventMap[EVENT_1]?.values?.size)

        rudderStack.aggregateData(aggregateDataKey = EVENT_1, attributeName = ATTRIBUTE_2, DATA_2)
        assertEquals(1, rudderStack.eventMap.size)
        assertEquals(2, rudderStack.eventMap[EVENT_1]?.values?.size)
    }

    @Test
    fun adding_multiple_events_to_aggregated_data() {
        rudderStack.aggregateData(aggregateDataKey = EVENT_1, attributeName = ATTRIBUTE_1, DATA_1)
        assertEquals(1, rudderStack.eventMap.size)
        assertEquals(1, rudderStack.eventMap[EVENT_1]?.values?.size)

        rudderStack.aggregateData(aggregateDataKey = EVENT_2, attributeName = ATTRIBUTE_2, DATA_2)
        assertEquals(2, rudderStack.eventMap.size)
        assertEquals(1, rudderStack.eventMap[EVENT_1]?.values?.size)
        assertEquals(1, rudderStack.eventMap[EVENT_2]?.values?.size)
    }


    @Test
    fun send_aggregated_data_event() {
        rudderStack.aggregateData(aggregateDataKey = EVENT_1, attributeName = ATTRIBUTE_1, DATA_1)
        rudderStack.aggregateData(aggregateDataKey = EVENT_2, attributeName = ATTRIBUTE_2, DATA_2)
        rudderStack.track(EVENT_1)
        assertEquals(1, rudderStack.eventMap.size)
        val remainingKey = rudderStack.eventMap.entries.find { it.key == EVENT_2 }?.key
        assertEquals(EVENT_2, remainingKey)

        val keyNotFound = rudderStack.eventMap.entries.find { it.key == EVENT_1 }?.key
        assertEquals(null, keyNotFound)
    }

    @Test
    fun send_aggregate_data_add_to_existing() {
        rudderStack.aggregateData(aggregateDataKey = EVENT_1, attributeName = ATTRIBUTE_1, DATA_INT)
        assertEquals(1, (rudderStack.eventMap[EVENT_1]?.get(ATTRIBUTE_1) as Int))
        rudderStack.aggregateData(
            aggregateDataKey = EVENT_1,
            attributeName = ATTRIBUTE_1,
            DATA_INT,
            CDPHelper.AdditionalAction.ADD_TO_EXISTING
        )
        assertEquals(2, (rudderStack.eventMap[EVENT_1]?.get(ATTRIBUTE_1) as Int))
    }

    @Test
    fun send_aggregate_data_overwrite_existing() {
        rudderStack.aggregateData(aggregateDataKey = EVENT_1, attributeName = ATTRIBUTE_1, DATA_INT)
        assertEquals(1, (rudderStack.eventMap[EVENT_1]?.get(ATTRIBUTE_1) as Int))
        rudderStack.aggregateData(
            aggregateDataKey = EVENT_1,
            attributeName = ATTRIBUTE_1,
            DATA_OVERWRITE,
            CDPHelper.AdditionalAction.OVERWRITE
        )
        assertEquals(DATA_OVERWRITE, (rudderStack.eventMap[EVENT_1]?.get(ATTRIBUTE_1) as Int))
    }


    @After
    fun stop() {
        closeable.close()
    }

    companion object {
        const val EVENT_1 = "EVENT1"
        const val EVENT_2 = "EVENT2"
        const val ATTRIBUTE_1 = "longitude"
        const val ATTRIBUTE_2 = "latitude"
        const val DATA_1 = "12.0000"
        const val DATA_2 = "55.0000"
        const val DATA_INT = 1
        const val DATA_OVERWRITE = 10
        const val URL = "ww.bums.dk"
        const val WRITE_KEY = "131kmff31f1f"

    }
}