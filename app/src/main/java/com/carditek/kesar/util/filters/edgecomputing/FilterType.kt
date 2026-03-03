package com.carditek.kesar.util.filters.edgecomputing

/**
 * Filter type enumeration for ECG filtering
 * Part of Edge Computing processing pipeline
 */
enum class FilterType {
    /**
     * Biquad IIR filter (current default)
     * - Fast, low latency
     * - Good for real-time processing
     * - 2nd order sections
     */
    BIQUAD,
    
    /**
     * FIR filter (Heavy filtering)
     * - Linear phase (no phase distortion)
     * - Guaranteed stability
     * - Better for ECG analysis
     * - Higher computation cost
     */
    FIR,
    
    /**
     * Butterworth IIR filter
     * - Maximally flat passband
     * - Sharp rolloff
     * - Higher order (4th-8th)
     * - Better noise rejection
     */
    BUTTERWORTH
}

