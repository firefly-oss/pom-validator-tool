package com.firefly.tools.pomvalidator.feature;

import com.firefly.tools.pomvalidator.model.ValidationResult;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

/**
 * Interface for formatting validation results into different output formats.
 * Implementations can support JSON, XML, Markdown, etc.
 */
public interface OutputFormatter {
    
    /**
     * Formats the validation results and returns the output as a string.
     *
     * @param results Map of POM paths to their validation results
     * @return Formatted string representation of the results
     */
    String format(Map<Path, ValidationResult> results);
    
    /**
     * Writes the formatted output to a specified file.
     *
     * @param results Map of POM paths to their validation results
     * @param outputFile Path to the output file
     * @throws IOException If an I/O error occurs
     */
    void write(Map<Path, ValidationResult> results, Path outputFile) throws IOException;
    
}

