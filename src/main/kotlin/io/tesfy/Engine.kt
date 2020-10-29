package io.tesfy

import io.tesfy.config.Config
import io.tesfy.config.Datafile
import io.tesfy.config.Experiment

class Engine(
    datafile: Datafile,
    var userId: String?,
    var attributes: Map<String, Any>,
    private val storage: Storage<String>,
    _cache: Map<String, String> = emptyMap()
) {

    private val config: Config = Config(datafile, TOTAL_BUCKETS)
    private val bucketer: Bucketer = Bucketer(TOTAL_BUCKETS)
    private val audienceEvaluator: AudienceEvaluator = AudienceEvaluator()
    val cache: MutableMap<String, String> = _cache.toMutableMap()

    fun setForcedVariation(experimentId: String, variationId: String) {
        cache[experimentId] = variationId
    }

    fun getVariationId(
        experimentId: String,
        userId: String?,
        attributes: Map<String, Any>?
    ): String? {
        var variationId: String? = getStaticVariation(experimentId) ?: storage.get(experimentId)

        if (variationId != null) {
            return variationId
        }

        val experiment: Experiment = config.getExperiment(experimentId) ?: return null
        val audience = experiment.audience

        if (!audienceEvaluator.evaluate(audience, attributes ?: this.attributes)) {
            return null
        }

        var key = computeKey(experimentId, userId, TRAFFIC_ALLOCATION_SALT)
        val allocation = config.getExperimentAllocation(experimentId)

        if (allocation == null || bucketer.bucket(key, listOf(allocation)) == null){
            return null
        }
        key = computeKey(experimentId, userId)
        val allocations = config.getExperimentAllocations(experimentId)
        variationId = bucketer.bucket(key, allocations)

        if (variationId != null) {
            storage.store(experimentId, variationId)
            return variationId
        }

        return null
    }

    private fun getStaticVariation(experimentId: String): String? = cache[experimentId]

    private fun computeKey(experimentId: String, userId: String? = "", trafficAllocationSalt: String = ""): String {
        return (userId ?: this.userId ?: "") + experimentId + trafficAllocationSalt
    }

    fun getVariationIds(
            userId: String,
            attributes: Map<String, Any>
    ): Map<String, String?> {
        val experiments = config.getExperiments()

        return experiments.keys.map {
                experimentId -> experimentId to getVariationId(experimentId, userId, attributes)
        }.toMap()
    }

    fun isFeatureEnabled(
        featureId: String,
        userId: String?,
        attributes: Map<String, Any>?
    ) : Boolean? {
        val feature = config.getFeature(featureId) ?: return null
        val key = computeKey(featureId, userId)
        val audience = feature.audience
        val allocation = config.getFeatureAllocation(featureId)

        if (allocation == null || !audienceEvaluator.evaluate(audience, attributes ?: this.attributes)) {
            return null
        }

        return bucketer.bucket(key, listOf(allocation))?.isNotEmpty() ?: false
    }

    fun getEnabledFeatures(
        userId: String?,
        attributes: Map<String, Any>?
    ): Map<String, Boolean?> {
        val features = config.getFeatures()

        return features.keys.map {
            it to isFeatureEnabled(it, userId, attributes)
        }.toMap()
    }

    companion object {
        const val TOTAL_BUCKETS = 10000
        const val TRAFFIC_ALLOCATION_SALT = "tas"
    }
}