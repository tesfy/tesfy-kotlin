package io.tesfy.unit.config

import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.shouldBe
import io.tesfy.Engine
import io.tesfy.config.*

class ConfigTest: WordSpec({

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
    val config = Config(datafile, Engine.TOTAL_BUCKETS)

    "getExperiments" should {
        "return all the experiments" {
            config.getExperiments() shouldBe datafile.experiments
        }
    }

    "getExperiment" should {
        "return an specific experiment" {
            config.getExperiment("experiment-1") shouldBe datafile.experiments["experiment-1"]
        }

        "return null if the experiment does not exist" {
            config.getExperiment("experiment-non-existent") shouldBe null
        }
    }

    "getFeatures" should {
        "return all the features" {
            config.getFeatures() shouldBe datafile.features
        }
    }

    "getFeature" should {
        "return an specific feature" {
            config.getFeature("feature-1") shouldBe datafile.features["feature-1"]
        }

        "return null if the feature does not exist" {
            config.getFeature("feature-42") shouldBe null
        }
    }

    "getFeatureAllocation" should {
        "return an allocation when the feature exists" {
            config.getFeatureAllocation("feature-1") shouldBe Allocation("feature-1", 5000)
        }

        "return null if the feature does not exist" {
            config.getFeatureAllocation("feature-2910") shouldBe null
        }
    }

    "getExperimentAllocations" should {
        "return a list of experiment allocations" {
            config.getExperimentAllocations("experiment-1") shouldBe listOf(
                Allocation("0", 5000),
                Allocation("1", 10000)
            )
        }

        "return an empty list of the experiment does not exist" {
            config.getExperimentAllocations("stopsendingnonexistantstuff") shouldBe emptyList()
        }
    }

    "getExperimentAllocation" should {
        "return an specific experiment allocation" {
            config.getExperimentAllocation("experiment-1") shouldBe Allocation("experiment-1", 10000)
        }

        "return null if the experiment does not exist" {
            config.getExperimentAllocation("okseriouslystahp") shouldBe null
        }
    }
})