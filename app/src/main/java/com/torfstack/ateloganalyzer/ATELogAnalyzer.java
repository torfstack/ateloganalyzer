package com.torfstack.ateloganalyzer;

import com.torfstack.ateloganalyzer.analysis.Analysis;
import com.torfstack.ateloganalyzer.analysis.AnalysisResult;
import com.torfstack.ateloganalyzer.analysis.TestEvent;
import com.torfstack.ateloganalyzer.input.Input;
import com.torfstack.ateloganalyzer.output.JsonFileSerializer;
import com.torfstack.ateloganalyzer.output.PrintConsoleSerializer;
import com.torfstack.ateloganalyzer.output.ResultSerializer;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Help.Ansi.Style;
import picocli.CommandLine.Help.ColorScheme;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Command(name = "ATELogAnalyzer", description = "Analyzes ATE log files for test durations and anomalies")
public class ATELogAnalyzer implements Runnable {

    @Option(names = {"-j", "--json"}, fallbackValue = "test_analysis.json", arity = "0..1",
            description = "Write results to JSON file (default: test_analysis.json)")
    private String jsonOutputFile;

    @Parameters(index = "0", description = "Log file to analyze")
    private String logFileToAnalyze;

    public static void main(String[] args) {
        int exitCode = new CommandLine(new ATELogAnalyzer())
                .setColorScheme(new ColorScheme.Builder()
                        .commands(Style.bold, Style.fg_blue)
                        .options(Style.fg_yellow)
                        .parameters(Style.fg_green)
                        .build())
                .execute(args);
        System.exit(exitCode);
    }

    @Override
    public void run() {
        List<TestEvent> events;
        try {
            events = Input.parseLogFile(logFileToAnalyze);
        } catch (IOException e) {
            System.err.println("Error reading log file: " + e.getMessage());
            return;
        }

        if (events.isEmpty()) {
            System.err.println("No valid test events found in the log file.");
        }

        AnalysisResult result = Analysis.analyzeTestTimes(events);

        Collection<ResultSerializer> serializers = new ArrayList<>();
        serializers.add(new PrintConsoleSerializer());
        if (jsonOutputFile != null && !jsonOutputFile.isEmpty()) {
            serializers.add(new JsonFileSerializer(jsonOutputFile));
        }

        for (ResultSerializer serializer : serializers) {
            try {
                serializer.serialize(result);
            } catch (Exception e) {
                System.err.println(e.getMessage());
                return;
            }
        }
    }
}