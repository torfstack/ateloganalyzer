package com.torfstack.ateloganalyzer.output;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.torfstack.ateloganalyzer.analysis.AnalysisResult;
import lombok.NonNull;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class JsonFileSerializer implements ResultSerializer {
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private final String filename;

    public JsonFileSerializer(String filename) {
        this.filename = filename;
    }

    @Override
    public void serialize(@NonNull AnalysisResult result) throws Exception {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            gson.toJson(result, writer);
            System.out.println("\nAnalysis results written to: " + filename);
        } catch (JsonIOException e) {
            throw new IOException("Failed to write JSON to file: " + filename, e);
        }
    }
}
