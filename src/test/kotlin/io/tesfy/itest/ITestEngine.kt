package io.tesfy.itest

import io.tesfy.Engine
import io.tesfy.Storage
import io.tesfy.config.Datafile
import io.tesfy.config.Experiment
import io.tesfy.config.Variation
import org.junit.Test

class StorageImpl : Storage<String> {
    val storage = mutableMapOf<String, String>()
    override fun get(id: String): String? {
        return storage[id]
    }

    override fun store(id: String, value: String) {
        storage[id] = value
    }
}

class ITestEngine {
    val datafile: Datafile = Datafile(
        mapOf(
            "exp1" to Experiment(
                "exp1",
                100.0,
                listOf(
                    Variation(
                        "0", 50.0
                    ),
                    Variation(
                        "1", 50.0
                    )
                ),
                mapOf(
                    "==" to """[{ var: 'countryCode' }, us]"""
                )
            )
        ),
        mapOf()
    )
    val userId = "abc123"
    val attributes = mapOf("countryCode" to "us")

    @Test
    fun testThatItWorks() {
        println("test: ${Engine(datafile, "abc123", attributes, StorageImpl()).getVariationId("exp1", "abc123", attributes)}")
        println("test: ${Engine(datafile, "abc1234", attributes, StorageImpl()).getVariationId("exp1", "abc1234", attributes)}")
        println("test: ${Engine(datafile, "abc1235", attributes, StorageImpl()).getVariationId("exp1", "abc12353211", attributes)}")
        println("test: ${Engine(datafile, "abc1236", attributes, StorageImpl()).getVariationId("exp1", "abc1236", attributes)}")
        println("test: ${Engine(datafile, "abc1237gyuioiyrewqaasdf", attributes, StorageImpl()).getVariationId("exp1", "abc1237gyuioiyrewqaasdf", attributes)}")
    }
}