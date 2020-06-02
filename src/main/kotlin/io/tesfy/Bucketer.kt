package io.tesfy

import io.tesfy.config.Allocation
import org.apache.commons.codec.digest.MurmurHash3
import kotlin.math.floor
import kotlin.math.pow

class Bucketer(private val maxBuckets: Int) {
    companion object {
        val HASH_SEED = 1
        val MAX_HASH_VALUE = 2.0.pow(32)
    }

    private fun computeBucketId(id: String): Long { // TODO: Is this a Long? Double? how much precision?
        val idBA = id.toByteArray()
        val hashValue = MurmurHash3.hash128x64(idBA, 0, idBA.size, HASH_SEED)
        val firstBits = hashValue[0]
        val ratio: Double = firstBits / MAX_HASH_VALUE

        return floor(ratio * maxBuckets.toDouble()).toLong()
    }

    fun bucket(key: String, allocations: List<Allocation>): String? {
        val bucketId = this.computeBucketId(key)
        val allocation = allocations.find { allocation -> bucketId < allocation.rangeEnd }

        return allocation?.id
    }
}