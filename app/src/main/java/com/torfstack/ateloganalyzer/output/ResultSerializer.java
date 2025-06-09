package com.torfstack.ateloganalyzer.output;

import com.torfstack.ateloganalyzer.analysis.AnalysisResult;
import lombok.NonNull;

public interface ResultSerializer {
    /**
     * Serializes the analysis result
     *
     * @param result The analysis result to serialize.
     */
    void serialize(@NonNull AnalysisResult result) throws Exception;
}
