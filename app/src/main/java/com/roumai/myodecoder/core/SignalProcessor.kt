package com.roumai.myodecoder.core

import com.github.psambit9791.jdsp.filter.Chebyshev

object SignalProcessor {
    fun filter(data: DoubleArray, samplingRate: Double, frequencyInterval: Pair<Double, Double>): DoubleArray {
        val filterType = 1
        val rippleFactor = 1.0
        val order = 4
        val flt = Chebyshev(samplingRate, rippleFactor, filterType)
        return flt.bandStopFilter(data, order, frequencyInterval.first, frequencyInterval.second)
    }
}