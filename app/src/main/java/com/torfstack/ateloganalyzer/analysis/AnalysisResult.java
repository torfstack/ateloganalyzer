package com.torfstack.ateloganalyzer.analysis;

import java.util.List;
import java.util.Map;

public record AnalysisResult(Map<String, TestStatistic> statistics, List<TestAnomaly> anomalies) {
}
