package com.torfstack.ateloganalyzer.output;

import com.torfstack.ateloganalyzer.analysis.AnalysisResult;
import lombok.NonNull;

public class PrintConsoleSerializer implements ResultSerializer {
    @Override
    public void serialize(@NonNull AnalysisResult result) {
        System.out.println("\nSubtest Duration Summaries:");
        System.out.println("----------------------");
        result.statistics().forEach((testType, stat) -> {
            System.out.printf("%-30s: Count=%d, Total=%d ms, Avg=%d ms, StdDev=%d ms%n",
                    testType, stat.count(), stat.totalMillis(), stat.avgMillis(), stat.stdDevMillis());
        });

        if (!result.anomalies().isEmpty()) {
            System.out.println("\nDetected Anomalies:");
            System.out.println("-------------------");
            result.anomalies().forEach(anomaly -> {
                System.out.printf("%s for %s took %d ms (average was %d ms, std. deviation was %d)%n",
                        anomaly.testType(),
                        anomaly.deviceInfo(),
                        anomaly.durationMillis(),
                        anomaly.averageMillis(),
                        anomaly.stdDevMillis());
            });
        }
    }
}
