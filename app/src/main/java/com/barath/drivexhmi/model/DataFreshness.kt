package com.barath.drivexhmi.model

/**
 * Tracks heartbeat and freshness of incoming CAN/telemetry stream.
 */
sealed interface DataFreshness {
    data object Fresh : DataFreshness
    data class Stale(val elapsedSinceLastSignalMs: Long) : DataFreshness
}

