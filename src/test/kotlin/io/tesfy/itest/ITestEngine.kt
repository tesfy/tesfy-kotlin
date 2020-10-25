package io.tesfy.itest

import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.shouldBe
import io.tesfy.Engine
import io.tesfy.Storage
import io.tesfy.config.Datafile
import io.tesfy.config.Experiment
import io.tesfy.config.Variation

private class StorageImpl : Storage<String> {
    val storage = mutableMapOf<String, String>()
    override fun get(id: String): String? {
        return storage[id]
    }

    override fun store(id: String, value: String) {
        storage[id] = value
    }
}

class ITestEngine : WordSpec({

    "getVariationId" should {
        val datafile = Datafile(
            mapOf(
                "experiment-1" to Experiment(
                    "experiment-1",
                    100,
                    listOf(
                        Variation(
                            "0", 50
                        ),
                        Variation(
                            "1", 50
                        )
                    ),
                    mapOf(
                        "==" to listOf(mapOf(
                            "var" to "countryCode"
                        ), "us")
                    )
                )
            ),
            mapOf()
        )
        lateinit var engine: Engine

        beforeEach {
            engine = Engine(datafile, null, emptyMap(), StorageImpl())
        }

        val attributes = mapOf("countryCode" to "us")
        val userId = "4qz936x2-62ex"

        "get a valid attribute of 0" {
            val variationId = engine.getVariationId("experiment-1", userId, attributes)

            variationId shouldBe "0"
        }

        "get a valid attribute of 1" {
            val userIdTest = "4qz936x2-62exacb"
            val variationId = engine.getVariationId("experiment-1", userIdTest, attributes)

            variationId shouldBe "1"
        }

        "test a datafile with audience defined as string" {
            val secondaryDatafile = Datafile(
                mapOf(
                    "experiment-1" to Experiment(
                        "experiment-1",
                        100,
                        listOf(
                            Variation(
                                "0", 50
                            ),
                            Variation(
                                "1", 50
                            )
                        ),
                        """{ "==": [{ var: 'countryCode' }, us]}"""
                    )
                ),
                mapOf()
            )
            val engineSecondary = Engine(secondaryDatafile, null, emptyMap(), StorageImpl())

            val userIdTest = "4qz936x2-62exacbc"
            val variationId = engineSecondary.getVariationId("experiment-1", userIdTest, attributes)

            variationId shouldBe "0"
        }

        "return null if the experiment does not exist" {
            engine.getVariationId("random", null, null) shouldBe null
        }

        "return null if the user is outside the audience" {
            val userIdTest = "4qz936x2-62exacbc"
            val attributesExcluded = mapOf("countryCode" to "ve")
            val variationId = engine.getVariationId("experiment-1", userIdTest, attributesExcluded)

            variationId shouldBe null
        }

        "return a static variationId from cache" {
            val engineSecondary = Engine(datafile, userId, attributes, StorageImpl(), mapOf("experiment-1" to "1"))
            val variationId = engineSecondary.getVariationId("experiment-1", userId, attributes)

            variationId shouldBe "1"
        }

        "return a static variationId from storage" {
            val storage = StorageImpl()
            storage.store("experiment-1", "1")
            val engineSecondary = Engine(datafile, userId, attributes, storage)
            val variationId = engineSecondary.getVariationId("experiment-1", userId, attributes)

            variationId shouldBe "1"
        }
    }

    "test getVariationIds " should {
        val datafile = Datafile(
            mapOf(
                "experiment-1" to Experiment(
                    "experiment-1",
                    100,
                    listOf(
                        Variation(
                            "0", 50
                        ),
                        Variation(
                            "1", 50
                        )
                    ),
                    mapOf(
                        "==" to listOf(mapOf(
                            "var" to "countryCode"
                        ), "nl")
                    )
                ),
                "experiment-2" to Experiment(
                    "experiment-2",
                    70,
                    listOf(
                        Variation(
                            "0", 30
                        ),
                        Variation(
                            "1", 70
                        )
                    ),
                    mapOf(
                        "==" to listOf(mapOf(
                            "var" to "countryCode"
                        ), "nl")
                    )
                )
            ),
            mapOf()
        )
        lateinit var engine: Engine

        beforeEach {
            engine = Engine(datafile, null, emptyMap(), StorageImpl())
        }

        val attributes = mapOf("countryCode" to "nl")
        val userId = "4qz936x2-62ex"

        "get the variation id from 2 different experiments" {
            val variationIds = engine.getVariationIds(userId, attributes)
            variationIds shouldBe mapOf("experiment-1" to "0", "experiment-2" to "1")
        }
    }
})