package com.torfstack.ateloganalyzer.analysis;

import lombok.NonNull;

import java.time.Duration;
import java.util.*;

public class Analysis {
    private static final int ACCEPTABLE_DEVIATION_MULTIPLIER = 2;

    public static @NonNull AnalysisResult analyzeTestTimes(@NonNull List<TestEvent> events) {
        Map<String, Collection<DeviceDuration>> durations = groupDurationsByTestType(events);
        Map<String, TestStatistic> statistics = calculateStatistics(durations);
        List<TestAnomaly> anomalies = findAnomalies(durations, statistics);
        return new AnalysisResult(statistics, anomalies);
    }

    private record DeviceDuration(String deviceInfo, Duration duration) {
    }

    private static Map<String, Collection<DeviceDuration>> groupDurationsByTestType(List<TestEvent> events) {
        Map<String, Collection<DeviceDuration>> durations = new LinkedHashMap<>();
        for (TestEvent event : events) {
            durations
                    .computeIfAbsent(event.eventType(), k -> new ArrayList<>())
                    .add(new DeviceDuration(event.deviceInfo(), event.duration()));
        }
        return durations;
    }

    private static Map<String, TestStatistic> calculateStatistics(Map<String, Collection<DeviceDuration>> durations) {
        Map<String, TestStatistic> statistics = new LinkedHashMap<>();
        for (String testType : durations.keySet()) {
            Duration total = durations.get(testType)
                    .stream()
                    .map(deviceDuration -> deviceDuration.duration)
                    .reduce(Duration.ofMillis(0L), Duration::plus);
            Duration avg = total.dividedBy(durations.get(testType).size());
            Duration variance = durations.get(testType)
                    .stream()
                    .map(deviceDuration -> deviceDuration.duration.minus(avg))
                    .map(duration -> duration.multipliedBy(duration.toMillis()))
                    .reduce(Duration.ofMillis(0L), Duration::plus)
                    .dividedBy(durations.get(testType).size());
            Duration stdDev = Duration.ofMillis((long) Math.sqrt(variance.toMillis()));

            TestStatistic testTypeStatistic = new TestStatistic(
                    durations.get(testType).size(),
                    total.toMillis(),
                    avg.toMillis(),
                    stdDev.toMillis()
            );
            statistics.put(testType, testTypeStatistic);
        }
        return statistics;
    }

    private static List<TestAnomaly> findAnomalies(Map<String, Collection<DeviceDuration>> durations, Map<String, TestStatistic> statistics) {
        List<TestAnomaly> anomalies = new ArrayList<>();
        for (String testType : durations.keySet()) {
            TestStatistic statisticsForTest = statistics.get(testType);
            Collection<DeviceDuration> deviceDurations = durations.get(testType);
            for (DeviceDuration deviceDuration : deviceDurations) {
                Duration differenceToAverage = deviceDuration.duration.minus(Duration.ofMillis(statisticsForTest.avgMillis()));
                if (differenceToAverage.isNegative()) differenceToAverage = differenceToAverage.negated();
                if (Math.abs(differenceToAverage.toMillis()) > ACCEPTABLE_DEVIATION_MULTIPLIER * statisticsForTest.stdDevMillis()) {
                    anomalies.add(new TestAnomaly(
                            testType,
                            deviceDuration.deviceInfo,
                            deviceDuration.duration.toMillis(),
                            statisticsForTest.avgMillis(),
                            statisticsForTest.stdDevMillis()
                    ));
                }
            }
        }
        return anomalies;
    }
}
