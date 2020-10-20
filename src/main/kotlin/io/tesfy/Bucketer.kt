package io.tesfy

import io.tesfy.config.Allocation
import org.apache.commons.codec.digest.MurmurHash3
import kotlin.math.floor
import kotlin.math.pow

class Bucketer(private val maxBuckets: Int) {
    companion object {
        val HASH_SEED = 1
        val MAX_HASH_VALUE = 2.0.pow(32) - 1
    }

    private fun computeBucketId(id: String): Long {
        val idBA = id.toByteArray()
        val hashValue = MurmurHash3.hash32x86(idBA, 0, idBA.size, HASH_SEED) //TODO: Check if values are correct
        val ratio: Double = hashValue / MAX_HASH_VALUE

        return floor(ratio * maxBuckets.toDouble()).toLong()
    }

    fun bucket(key: String, allocations: List<Allocation>): String? {
        val bucketId = this.computeBucketId(key)
        val allocation = allocations.find { allocation -> bucketId < allocation.rangeEnd }

        return allocation?.id
    }
}