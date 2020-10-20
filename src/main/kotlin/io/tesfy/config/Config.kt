package io.tesfy.config

import kotlin.math.floor

class Config(
    private val datafile: Datafile,
    private val maxBuckets: Int
) {

    private fun computeRangeEnd(percentage: Double): Double = floor((this.maxBuckets * percentage) / 100)

    fun getExperiments(): Map<String, Experiment> = this.datafile.experiments

    fun getExperiment(id: String): Experiment? = this.datafile.experiments[id]

    fun getFeatures(): Map<String, Feature> = this.datafile.features

    fun getFeature(id: String): Feature? = this.datafile.features[id]

    fun getFeatureAllocation(id: String): Allocation? {
        val feature = this.getFeature(id) ?: return null
        val rangeEnd = this.computeRangeEnd(feature.percentage)

        return Allocation(id, rangeEnd)
    }

    fun getExperimentAllocations(id: String): List<Allocation> {
        val experiment = this.getExperiment(id) ?: return emptyList()
        var acc = 0.0

        return experiment.variations.map { variation ->
            acc += variation.percentage
            val rangeEnd = acc * computeRangeEnd(experiment.percentage)

            Allocation(variation.id, rangeEnd)
        }
    }

    fun getExperimentAllocation(experimentId: String): Allocation? {
        val experiment = getExperiment(experimentId) ?: return null
        val rangeEnd = computeRangeEnd(experiment.percentage)

        return Allocation(experimentId, rangeEnd)
    }
}