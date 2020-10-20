package io.tesfy

import io.github.jamsesso.jsonlogic.JsonLogic
import kotlinx.serialization.*
import kotlinx.serialization.json.*

class AudienceEvaluator {
    private val jsonLogic: JsonLogic = JsonLogic()

    fun evaluate(audience: Map<String, String>, attributes: Map<String, String>): Boolean {
        val jsonLogicString: String = Json.encodeToString(audience)
        val logic2 = """{"==":[{"var":"countryCode"}, "us"]}""" //TODO: Add proper serializer
        return jsonLogic.apply(logic2, attributes) as Boolean
    }

}
