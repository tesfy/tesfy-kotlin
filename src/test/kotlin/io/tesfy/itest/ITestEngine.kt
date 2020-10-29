package io.tesfy.itest

import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.shouldBe
import io.tesfy.Engine
import io.tesfy.Storage
import io.tesfy.config.Datafile
import io.tesfy.config.Experiment
import io.tesfy.config.Feature
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

    "isFeatureEnabled" should {
        lateinit var engine: Engine
        val datafile = Datafile(
            mapOf(),
            mapOf(
                "feature-1" to Feature(
                    "feature-1",
                    50,
                    mapOf(
                        "==" to listOf(mapOf(
                            "var" to "countryCode"
                        ), "nl")
                    )
                ),
                "feature-2" to Feature(
                    "feature-2",
                    50,
                    mapOf(
                        "==" to listOf(mapOf(
                            "var" to "countryCode"
                        ), "us")
                    )
                )
            )
        )
        val userIdTest = "4qz936x2-62exacbc"

        beforeEach {
            engine = Engine(datafile, null, emptyMap(), StorageImpl())
        }

        "get an enabled feature" {
            val attributes = mapOf("countryCode" to "nl")
            val isFeatureEnabled = engine.isFeatureEnabled("feature-1", userIdTest, attributes)
            isFeatureEnabled shouldBe true
        }

        "get a disabled feature" {
            val attributes = mapOf("countryCode" to "us")
            val isFeatureEnabled = engine.isFeatureEnabled("feature-2", userIdTest, attributes)
            isFeatureEnabled shouldBe false
        }

        "return null if the feature does not exist" {
            engine.isFeatureEnabled("asd", userIdTest, emptyMap()) shouldBe null
        }

        "return null if the attribute does not match" {
            engine.isFeatureEnabled("feature-1", userIdTest, emptyMap()) shouldBe null
        }
    }

    "getEnabledFeatures" should {
        lateinit var engine: Engine
        val datafile = Datafile(
            mapOf(),
            mapOf(
                "feature-1" to Feature(
                    "feature-1",
                    50,
                    mapOf(
                        "==" to listOf(mapOf(
                            "var" to "countryCode"
                        ), "nl")
                    )
                ),
                "feature-2" to Feature(
                    "feature-2",
                    50,
                    mapOf(
                        "==" to listOf(mapOf(
                            "var" to "countryCode",
                            "var" to "secondCountry"
                        ), "us")
                    )
                ),
                "feature-3" to Feature(
                    "feature-3",
                    50,
                    mapOf(
                        "==" to listOf(mapOf(
                            "var" to "countryCode"
                        ), "xx")
                    )
                )
            )
        )
        val userIdTest = "4qz936x2-62exacbc"

        beforeEach {
            engine = Engine(datafile, null, emptyMap(), StorageImpl())
        }

        "get a set of features" {
            val attributes = mapOf("countryCode" to "nl", "secondCountry" to "us")
            val enabledFeatures = engine.getEnabledFeatures(userIdTest, attributes)
            enabledFeatures shouldBe mapOf("feature-1" to true, "feature-2" to false, "feature-3" to null)
        }
    }

    "getVariationIds" should {
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

    "setForcedVariation" should {
        val datafile = Datafile(emptyMap(), emptyMap())
        val engine = Engine(datafile, null, emptyMap(), StorageImpl())

        "set a variation in cache sucessfully" {
            engine.setForcedVariation("exp-23-06-1991",  "variation-sth")
            engine.cache["exp-23-06-1991"] shouldBe "variation-sth"
        }
    }
})