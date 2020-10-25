package io.tesfy.unit

import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.shouldBe
import io.tesfy.Bucketer
import io.tesfy.Engine
import io.tesfy.config.Allocation

class BucketerTest: WordSpec({
    val bucketer = Bucketer(Engine.TOTAL_BUCKETS)
    "bucket function" should {
        val userId0 = "4qz936x2-62exexperiment-1tas"
        val userId1 = "4qz936x2-62exacbexperiment-1tas"
        val allocations = listOf(
            Allocation("0", 5000L),
            Allocation("1", 10000L)
        )
        val bucket0 = bucketer.bucket(userId0, allocations)
        val bucket1 = bucketer.bucket(userId1, allocations)

        bucket0 shouldBe "0"
        bucket1 shouldBe "1"
    }
})