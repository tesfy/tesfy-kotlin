package io.tesfy

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import io.github.jamsesso.jsonlogic.JsonLogic

class AudienceEvaluator {
    private val jsonLogic: JsonLogic = JsonLogic()
    private val mapper: ObjectMapper = ObjectMapper().registerModule(KotlinModule())

    fun evaluate(audience: Any, attributes: Map<String, String>): Boolean {
        val jsonLogicString = processAudience(audience)
        return jsonLogic.apply(jsonLogicString, attributes) as Boolean
    }

    private fun processAudience(audience: Any): String =
        if (audience is String) {
            audience
        } else {
            mapper.writeValueAsString(audience)
        }
}
