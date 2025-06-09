package com.torfstack.ateloganalyzer.analysis;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AnalysisTest {

    @Test
    void analyzeTestTimes_EmptyList_ReturnsEmptyResult() {
        // Arrange
        List<TestEvent> events = List.of();

        // Act
        AnalysisResult result = Analysis.analyzeTestTimes(events);

        // Assert
        assertNotNull(result);
        assertTrue(result.statistics().isEmpty());
        assertTrue(result.anomalies().isEmpty());
    }

    @Test
    void analyzeTestTimes_SingleTestType_CalculatesCorrectStatistics() {
        // Arrange
        List<TestEvent> events = List.of(
                createTestEvent("test1", "device1", Duration.ofSeconds(10)),
                createTestEvent("test1", "device2", Duration.ofSeconds(12)),
                createTestEvent("test1", "device3", Duration.ofSeconds(14))
        );

        // Act
        AnalysisResult result = Analysis.analyzeTestTimes(events);

        // Assert
        assertEquals(1, result.statistics().size());
        TestStatistic stats = result.statistics().get("test1");
        assertNotNull(stats);
        assertEquals(3, stats.count());
        assertEquals(12000, stats.avgMillis());
        assertEquals(36000, stats.totalMillis());
        assertTrue(result.anomalies().isEmpty());
    }

    @Test
    void analyzeTestTimes_DetectsAnomalies() {
        // Arrange
        List<TestEvent> events = List.of(
                createTestEvent("test1", "device1", Duration.ofSeconds(5)),
                createTestEvent("test1", "device2", Duration.ofSeconds(5)),
                createTestEvent("test1", "device3", Duration.ofSeconds(4)),
                createTestEvent("test1", "device4", Duration.ofSeconds(6)),
                createTestEvent("test1", "device5", Duration.ofSeconds(20)), // Anomaly
                createTestEvent("test1", "device6", Duration.ofSeconds(5)),
                createTestEvent("test1", "device7", Duration.ofSeconds(5)),
                createTestEvent("test1", "device8", Duration.ofSeconds(4)),
                createTestEvent("test1", "device9", Duration.ofSeconds(6))
        );

        // Act
        AnalysisResult result = Analysis.analyzeTestTimes(events);

        // Assert
        assertFalse(result.anomalies().isEmpty());
        assertEquals(1, result.anomalies().size());
        TestAnomaly anomaly = result.anomalies().getFirst();
        assertEquals("test1", anomaly.testType());
        assertEquals("device5", anomaly.deviceInfo());
        assertEquals(20000, anomaly.durationMillis());
    }

    @Test
    void analyzeTestTimes_MultipleTestTypes_CalculatesCorrectStatistics() {
        // Arrange
        List<TestEvent> events = List.of(
                createTestEvent("test1", "device1", Duration.ofSeconds(10)),
                createTestEvent("test2", "device1", Duration.ofSeconds(20)),
                createTestEvent("test1", "device2", Duration.ofSeconds(10)),
                createTestEvent("test2", "device2", Duration.ofSeconds(30))
        );

        // Act
        AnalysisResult result = Analysis.analyzeTestTimes(events);

        // Assert
        assertEquals(2, result.statistics().size());
        TestStatistic test1Stats = result.statistics().get("test1");
        assertEquals(10000, test1Stats.avgMillis());
        assertEquals(0, test1Stats.stdDevMillis());
        TestStatistic test2Stats = result.statistics().get("test2");
        assertEquals(25000, test2Stats.avgMillis());
        assertEquals(5000, test2Stats.stdDevMillis());
        assertTrue(result.anomalies().isEmpty());
    }

    private TestEvent createTestEvent(String testType, String deviceInfo, Duration duration) {
        return new TestEvent(
                null, // startTime not needed for analysis yet
                null, // endTime not needed for analysis yet
                duration,
                testType,
                deviceInfo
        );
    }
}