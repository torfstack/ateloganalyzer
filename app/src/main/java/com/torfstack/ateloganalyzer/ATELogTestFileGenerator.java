package com.torfstack.ateloganalyzer;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Random;

import static com.torfstack.ateloganalyzer.constants.Constants.TIME_FORMATTER;

public class ATELogTestFileGenerator {
    private static final String[] SUBTESTS = {
            "check connection",
            "measure leakage current",
            "test digital logic",
            "verify analog circuits",
            "calibrate sensors",
            "validate memory",
            "stress test"
    };
    private static final Random random = new Random();

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: java ATELogGenerator <output_file> <num_devices>");
            System.out.println("Example: java ATELogGenerator test_log.txt 1000");
            return;
        }

        String outputFile = args[0];
        int numDevices = Integer.parseInt(args[1]);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {
            for (int i = 1; i <= numDevices; i++) {
                generateDeviceTest(writer, i);

                if (i % 100 == 0) {
                    System.out.printf("Generated logs for %d/%d devices%n", i, numDevices);
                }
            }
            System.out.println("Log generation complete. File: " + outputFile);
        } catch (IOException e) {
            System.err.println("Error writing log file: " + e.getMessage());
        }
    }

    private static void generateDeviceTest(BufferedWriter writer, int deviceId) throws IOException {
        LocalTime currentTime = LocalTime.of(8, 0).plusSeconds(deviceId * 10L);

        // Insertion phase
        writeLogEntry(writer, currentTime, "BEGIN INSERTION \"device #" + deviceId + "\"");
        currentTime = currentTime.plus(500 + random.nextInt(1500), ChronoUnit.MILLIS);
        writeLogEntry(writer, currentTime, "END INSERTION");

        // Device test
        currentTime = currentTime.plus(100 + random.nextInt(400), ChronoUnit.MILLIS);
        writeLogEntry(writer, currentTime, "BEGIN DEVICE_TEST");

        // Subtests
        for (String subtest : SUBTESTS) {
            currentTime = currentTime.plus(50 + random.nextInt(200), ChronoUnit.MILLIS);
            writeLogEntry(writer, currentTime, "BEGIN DEVICE_TEST.SUBTEST \"" + subtest + "\"");

            // Random duration with occasional long delays
            long duration;
            if (random.nextInt(100) < 2) { // 2% chance long delay in subtest
                duration = 5000 + random.nextInt(5000); // Long delay (5-10s)
            } else {
                duration = 50 + random.nextInt(150); // Normal delay (50-200ms)
            }

            currentTime = currentTime.plus(duration, ChronoUnit.MILLIS);
            writeLogEntry(writer, currentTime, "END DEVICE_TEST.SUBTEST");
        }

        // End device test
        currentTime = currentTime.plus(100 + random.nextInt(400), ChronoUnit.MILLIS);
        writeLogEntry(writer, currentTime, "END DEVICE_TEST");

        // Removal phase
        currentTime = currentTime.plus(50 + random.nextInt(200), ChronoUnit.MILLIS);
        writeLogEntry(writer, currentTime, "BEGIN REMOVAL");
        currentTime = currentTime.plus(300 + random.nextInt(700), ChronoUnit.MILLIS);
        writeLogEntry(writer, currentTime, "END REMOVAL");
    }

    private static void writeLogEntry(BufferedWriter writer, LocalTime time, String message) throws IOException {
        writer.write(String.format("#PE[%s] : %s%n", time.format(TIME_FORMATTER), message));
    }
}
