package com.barath.drivexhmi.model

/**
 * Vehicle ride modes inspired by modern electric vehicle digital clusters.
 */
enum class RideMode(val displayName: String) {
    PARK("PARK"),
    ECO("ECO"),
    RIDE("RIDE"),
    SONIC("SONIC"),
    EMERGENCY("DISABLED")
}

