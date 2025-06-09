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
import java.util.Optional;
import java.util.stream.Stream;

import static com.torfstack.ateloganalyzer.constants.Constants.TIME_FORMATTER;

public class Input {

    private static class ParseContext {
        String currentDevice = "Unknown Device";
        String currentSubtest = "Unknown Subtest";
        LocalTime currentBeginTimestamp = null;
    }

    public static @NonNull List<TestEvent> parseLogFile(@NonNull String filename) throws IOException {
        List<TestEvent> events = new ArrayList<>();

        try (Stream<String> lines = Files.lines(Paths.get(filename))) {
            ParseContext context = new ParseContext();
            lines.forEach(line -> {
                line = line.trim();
                if (isValidLine(line)) {
                    parseEvent(line, context).ifPresent(events::add);
                }
            });
        }

        return events;
    }

    private static boolean isValidLine(@NonNull String line) {
        return !line.trim().isEmpty() && line.startsWith("#PE");
    }

    private static @NonNull Optional<TestEvent> parseEvent(@NonNull String line, @NonNull ParseContext context) {
        int timeStart = line.indexOf('[') + 1;
        int timeEnd = line.indexOf(']');
        String timeStr = line.substring(timeStart, timeEnd);
        LocalTime timestamp = LocalTime.parse(timeStr, TIME_FORMATTER);

        int descStart = line.indexOf(": ") + 2;
        String description = line.substring(descStart);

        String deviceInfo = description.contains("\"device") ?
                description.substring(description.indexOf("\"")+1, description.lastIndexOf("\"")) : "";
        if (!deviceInfo.isEmpty()) {
            context.currentDevice = deviceInfo;
        }

        // not an event of Step 2, we only care about the subtest events
        if (!description.contains("DEVICE_TEST.SUBTEST")) {
            return Optional.empty();
        }

        boolean isBegin = description.startsWith("BEGIN");
        if (!isBegin) {
            Duration duration = Duration.between(context.currentBeginTimestamp, timestamp);
            TestEvent endEvent = new TestEvent(context.currentBeginTimestamp, timestamp, duration, context.currentSubtest, context.currentDevice);
            context.currentSubtest = "";
            context.currentBeginTimestamp = null;
            return Optional.of(endEvent);
        }

        context.currentSubtest =
                description.substring(description.indexOf("SUBTEST") + 9, description.lastIndexOf("\""));
        context.currentBeginTimestamp = timestamp;
        return Optional.empty();
    }
}
