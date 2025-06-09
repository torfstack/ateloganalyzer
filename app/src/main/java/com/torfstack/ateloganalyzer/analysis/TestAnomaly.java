package com.torfstack.ateloganalyzer.analysis;

public record TestAnomaly(String testType, String deviceInfo, long durationMillis, long averageMillis,
                          long stdDevMillis) {
}
