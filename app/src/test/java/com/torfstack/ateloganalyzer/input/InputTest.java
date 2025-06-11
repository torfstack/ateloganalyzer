package com.torfstack.ateloganalyzer.input;

import com.torfstack.ateloganalyzer.analysis.TestEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InputTest {

    @TempDir
    Path tempDir;

    @Test
    void parseLogFile_ValidLogFile_ReturnsTestEvents() throws Exception {
        // Arrange
        Path logFile = tempDir.resolve("valid.log");
        Files.writeString(logFile, """
            #PE[14:34:31.300] : BEGIN INSERTION "device #13"
            #PE[14:34:33.400] : END INSERTION
            #PE[14:34:33.500] : BEGIN DEVICE_TEST
            #PE[14:35:01.300] : BEGIN DEVICE_TEST.SUBTEST "check connection"
            #PE[14:35:01.600] : END DEVICE_TEST.SUBTEST
            #PE[14:35:01.800] : BEGIN DEVICE_TEST.SUBTEST "measure leakage current"
            #PE[14:35:02.100] : END DEVICE_TEST.SUBTEST
            #PE[14:36:02.300] : END DEVICE_TEST
            #PE[14:36:04.200] : BEGIN REMOVAL
            #PE[14:36:07.100] : END REMOVAL

            #PE[14:37:31.300] : BEGIN INSERTION "device #14"
            #PE[14:38:33.400] : END INSERTION
            #PE[14:38:33.500] : BEGIN DEVICE_TEST
            #PE[14:39:01.300] : BEGIN DEVICE_TEST.SUBTEST "check connection"
            #PE[14:39:01.600] : END DEVICE_TEST.SUBTEST
            #PE[14:39:01.700] : BEGIN DEVICE_TEST.SUBTEST "measure leakage current"
            #PE[14:39:02.200] : END DEVICE_TEST.SUBTEST
            #PE[14:40:02.300] : END DEVICE_TEST
            #PE[14:40:04.200] : BEGIN REMOVAL
            #PE[14:40:07.100] : END REMOVAL
        """);

        // Act
        List<TestEvent> events = Input.parseLogFile(logFile.toString());

        // Assert
        assertNotNull(events);
        assertEquals(4, events.size());
    }

    @Test
    void parseLogFile_EmptyFile_ReturnsEmptyList() throws Exception {
        // Arrange
        Path logFile = tempDir.resolve("empty.log");
        Files.writeString(logFile, "");

        // Act
        List<TestEvent> events = Input.parseLogFile(logFile.toString());

        // Assert
        assertNotNull(events);
        assertTrue(events.isEmpty());
    }

    @Test
    void parseLogFile_NonexistentFile_ThrowsException() {
        assertThrows(IOException.class, () ->
                Input.parseLogFile("nonexistent.log")
        );
    }

    @Test
    void parseLogFile_InvalidFormat_ReturnsEmptyList() throws Exception {
        // Arrange
        Path logFile = tempDir.resolve("invalid.log");
        Files.writeString(logFile, "Invalid content\nMore invalid content");

        // Act
        List<TestEvent> events = Input.parseLogFile(logFile.toString());

        // Assert
        assertNotNull(events);
        assertTrue(events.isEmpty());
    }

    @Test
    void parseLogFile_EndSubtestWithoutBegin_SkipsEvent() throws Exception {
        // Arrange
        Path logFile = tempDir.resolve("endWithoutBegin.log");
        Files.writeString(logFile, """
        #PE[14:35:01.600] : END DEVICE_TEST.SUBTEST
        """);

        // Act
        List<TestEvent> events = Input.parseLogFile(logFile.toString());

        // Assert
        assertNotNull(events);
        assertTrue(events.isEmpty());
    }

    @Test
    void parseLogFile_BeginSubtestWhileActive_SkipsEvent() throws Exception {
        // Arrange
        Path logFile = tempDir.resolve("beginWhileActive.log");
        Files.writeString(logFile, """
        #PE[14:35:01.300] : BEGIN DEVICE_TEST.SUBTEST "check connection"
        #PE[14:35:01.400] : BEGIN DEVICE_TEST.SUBTEST "measure leakage current"
        #PE[14:35:01.600] : END DEVICE_TEST.SUBTEST
        """);

        // Act
        List<TestEvent> events = Input.parseLogFile(logFile.toString());

        // Assert
        assertNotNull(events);
        assertEquals(1, events.size());
        assertEquals("check connection", events.get(0).eventType());
    }
}