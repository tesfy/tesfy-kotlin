package io.tesfy

import io.tesfy.config.Allocation
import org.apache.commons.codec.digest.MurmurHash3
import kotlin.math.floor
import kotlin.math.pow

class Bucketer(private val maxBuckets: Int) {
    companion object {
        const val HASH_SEED = 1
        val MAX_HASH_VALUE = 2.0.pow(32) - 1
    }

    @ExperimentalUnsignedTypes
    private fun computeBucketId(id: String): Long {
        val idBA = id.toByteArray()
        val hashValue: UInt = MurmurHash3.hash32x86(idBA, 0, idBA.size, HASH_SEED).toUInt()
        val ratio: Double = hashValue.toDouble() / MAX_HASH_VALUE

        return floor(ratio * maxBuckets).toLong()
    }

    fun bucket(key: String, allocations: List<Allocation>): String? {
        val bucketId = this.computeBucketId(key)
        val allocation = allocations.find { allocation -> bucketId < allocation.rangeEnd }

        return allocation?.id
    }
}