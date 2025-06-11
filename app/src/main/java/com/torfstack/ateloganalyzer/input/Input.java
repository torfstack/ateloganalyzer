package com.torfstack.ateloganalyzer.input;

import com.torfstack.ateloganalyzer.analysis.TestEvent;
import lombok.NonNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static com.torfstack.ateloganalyzer.constants.Constants.TIME_FORMATTER;

public class Input {

    private static final Pattern TIMESTAMP_PATTERN = Pattern.compile("#PE\\[(.+)]");
    private static final Pattern DEVICE_PATTERN = Pattern.compile("BEGIN INSERTION \"(.+)\"");
    private static final Pattern SUBTEST_NAME_PATTERN = Pattern.compile("DEVICE_TEST.SUBTEST \"(.+)\"");

    private static class ParseContext {
        String currentDevice = "Unknown Device";
        String currentSubtest = "Unknown Subtest";
        LocalTime currentBeginTimestamp = null;

        void clearSubtest() {
            currentSubtest = "Unknown Subtest";
            currentBeginTimestamp = null;
        }

        boolean isSubtestActive() {
            return currentBeginTimestamp != null || !currentSubtest.equals("Unknown Subtest");
        }
    }

    public static @NonNull List<TestEvent> parseLogFile(@NonNull String filename) throws IOException {
        List<TestEvent> events = new ArrayList<>();
        ParseContext context = new ParseContext();

        try (Stream<String> lines = Files.lines(Paths.get(filename))) {
            lines.forEach(line -> {
                Matcher deviceMatcher = DEVICE_PATTERN.matcher(line);
                context.currentDevice = deviceMatcher.find() ? deviceMatcher.group(1) : context.currentDevice;

                Matcher subtestMatcher = SUBTEST_NAME_PATTERN.matcher(line);
                String currentSubtest = subtestMatcher.find() ? subtestMatcher.group(1) : context.currentSubtest;

                Matcher timestampMatcher = TIMESTAMP_PATTERN.matcher(line);
                String timestampString = timestampMatcher.find() ? timestampMatcher.group(1) : null;
                if (timestampString == null) return;
                LocalTime timestamp = LocalTime.parse(timestampString, TIME_FORMATTER);

                boolean isBeginSubtest = line.contains("BEGIN DEVICE_TEST.SUBTEST");
                boolean isEndSubtest = line.contains("END DEVICE_TEST.SUBTEST");

                if (isBeginSubtest) {
                    if (context.isSubtestActive()) {
                        System.err.println("Warning: BEGIN DEVICE_TEST.SUBTEST without matching END at " + timestamp);
                        return; // Skip this event if a subtest is already active
                    }
                    context.currentBeginTimestamp = timestamp;
                    context.currentSubtest = currentSubtest;
                } else if (isEndSubtest) {
                    if (context.currentBeginTimestamp == null) {
                        System.err.println("Warning: END DEVICE_TEST.SUBTEST without matching BEGIN at " + timestamp);
                        return; // Skip this event if no begin timestamp is set
                    }
                    TestEvent event = new TestEvent(context.currentBeginTimestamp, timestamp,
                            Duration.between(context.currentBeginTimestamp, timestamp),
                            context.currentSubtest, context.currentDevice);
                    events.add(event);
                    context.clearSubtest();
                }
            });
        }

        return events;
    }
}
