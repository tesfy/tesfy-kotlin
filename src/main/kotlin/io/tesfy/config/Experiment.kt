package io.tesfy.config

data class Experiment(
    val id: String,
    val percentage: Double,
    val variations: List<Variation>,
    val audience: Map.Entry<String, Any>
)