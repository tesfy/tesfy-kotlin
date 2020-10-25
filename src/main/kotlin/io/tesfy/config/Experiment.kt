package io.tesfy.config

data class Experiment(
    val id: String,
    val percentage: Int,
    val variations: List<Variation>,
    val audience: Any
)

