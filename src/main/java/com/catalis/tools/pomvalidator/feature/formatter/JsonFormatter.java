package com.catalis.tools.pomvalidator.feature.formatter;

import com.catalis.tools.pomvalidator.feature.OutputFormatter;
import com.catalis.tools.pomvalidator.model.ValidationResult;
import com.catalis.tools.pomvalidator.model.ValidationIssue;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * Formats validation results as JSON output.
 * Provides structured data suitable for programmatic consumption.
 */
public class JsonFormatter implements OutputFormatter {
    
    private final ObjectMapper mapper;
    
    public JsonFormatter() {
        this.mapper = new ObjectMapper();
        this.mapper.enable(SerializationFeature.INDENT_OUTPUT);
    }
    
    @Override
    public String format(Map<Path, ValidationResult> results) {
        ObjectNode root = mapper.createObjectNode();
        
        // Metadata
        root.put("tool", "POM Validator Tool");
        root.put("version", "1.0.0-SNAPSHOT");
        root.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        root.put("totalPoms", results.size());
        
        // Summary statistics
        ObjectNode summary = mapper.createObjectNode();
        int totalErrors = 0;
        int totalWarnings = 0;
        int totalInfos = 0;
        int validCount = 0;
        
        for (ValidationResult result : results.values()) {
            totalErrors += result.getErrors().size();
            totalWarnings += result.getWarnings().size();
            totalInfos += result.getInfos().size();
            if (result.isValid()) {
                validCount++;
            }
        }
        
        summary.put("validPoms", validCount);
        summary.put("invalidPoms", results.size() - validCount);
        summary.put("totalErrors", totalErrors);
        summary.put("totalWarnings", totalWarnings);
        summary.put("totalInfos", totalInfos);
        root.set("summary", summary);
        
        // Detailed results
        ArrayNode resultsArray = mapper.createArrayNode();
        for (Map.Entry<Path, ValidationResult> entry : results.entrySet()) {
            ObjectNode pomResult = mapper.createObjectNode();
            pomResult.put("file", entry.getKey().toString());
            pomResult.put("valid", entry.getValue().isValid());
            
            // Add errors
            ArrayNode errors = mapper.createArrayNode();
            for (ValidationIssue issue : entry.getValue().getErrors()) {
                errors.add(createIssueNode(issue));
            }
            pomResult.set("errors", errors);
            
            // Add warnings
            ArrayNode warnings = mapper.createArrayNode();
            for (ValidationIssue issue : entry.getValue().getWarnings()) {
                warnings.add(createIssueNode(issue));
            }
            pomResult.set("warnings", warnings);
            
            // Add infos
            ArrayNode infos = mapper.createArrayNode();
            for (ValidationIssue issue : entry.getValue().getInfos()) {
                infos.add(createIssueNode(issue));
            }
            pomResult.set("infos", infos);
            
            resultsArray.add(pomResult);
        }
        root.set("results", resultsArray);
        
        try {
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(root);
        } catch (Exception e) {
            throw new RuntimeException("Failed to format JSON output", e);
        }
    }
    
    private ObjectNode createIssueNode(ValidationIssue issue) {
        ObjectNode node = mapper.createObjectNode();
        node.put("message", issue.getMessage());
        if (issue.hasSuggestion()) {
            node.put("suggestion", issue.getSuggestion());
        }
        return node;
    }
    
    @Override
    public void write(Map<Path, ValidationResult> results, Path outputFile) throws IOException {
        String json = format(results);
        Files.writeString(outputFile, json);
    }
}
