package io.tesfy.unit

import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.shouldBe
import io.tesfy.AudienceEvaluator

class AudienceEvaluatorTest : WordSpec({
    val audienceEvaluator = AudienceEvaluator()

    "evaluate" should {

        "evaluate a jsonlogic defined as string" {
            audienceEvaluator.evaluate(
                """{ "==": [{ var: 'countryCode' }, us]}""",
                mapOf("countryCode" to "us")
            ) shouldBe true
        }

        "return true when jsonLogic is valid" {
            audienceEvaluator.evaluate(
                mapOf(
                    "==" to listOf(
                        mapOf("var" to "countryCode"),
                        "us"
                    )
                ),
                mapOf("countryCode" to "us")
            ) shouldBe true
        }

        "return false when jsonLogic is invalid" {
            audienceEvaluator.evaluate(
                mapOf(
                    "==" to listOf(
                        mapOf("var" to "countryCode"),
                        "nl"
                    )
                ),
                mapOf("countryCode" to "us")
            ) shouldBe false
        }
    }
})