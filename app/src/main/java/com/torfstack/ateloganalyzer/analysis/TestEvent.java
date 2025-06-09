package com.torfstack.ateloganalyzer.analysis;

import lombok.NonNull;

import java.time.Duration;
import java.time.LocalTime;

public record TestEvent(LocalTime begin, LocalTime end, Duration duration, String eventType, String deviceInfo) {

    @Override
    public @NonNull String toString() {
        return "TestEvent{" +
                "begin=" + begin +
                ", end=" + end +
                ", duration=" + duration +
                ", eventType='" + eventType + '\'' +
                ", deviceInfo='" + deviceInfo + '\'' +
                '}';
    }
}

