/*
 * Copyright 2025 Firefly Software Solutions Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package com.firefly.tools.pomvalidator.feature.formatter;

import com.firefly.tools.pomvalidator.model.ValidationResult;
import com.firefly.tools.pomvalidator.model.ValidationIssue;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class FormatterTest {
    
    @TempDir
    Path tempDir;
    
    private Map<Path, ValidationResult> testResults;
    
    @BeforeEach
    void setUp() {
        testResults = new HashMap<>();
        
        // Create test validation results
        ValidationResult result1 = new ValidationResult();
        result1.addError(ValidationIssue.of("Missing groupId", "Add <groupId>"));
        result1.addWarning(ValidationIssue.of("Missing encoding", "Add UTF-8 encoding"));
        result1.addInfo(ValidationIssue.of("GAV: null:test:1.0.0", null));
        
        ValidationResult result2 = new ValidationResult();
        result2.addWarning(ValidationIssue.of("SNAPSHOT dependency", "Use release version"));
        
        testResults.put(Path.of("pom.xml"), result1);
        testResults.put(Path.of("module/pom.xml"), result2);
    }
    
    @Test
    void testJsonFormatter() throws Exception {
        JsonFormatter formatter = new JsonFormatter();
        String json = formatter.format(testResults);
        
        assertNotNull(json, "JSON output should not be null");
        assertTrue(json.contains("\"tool\" : \"POM Validator Tool\""), "Should contain tool name");
        assertTrue(json.contains("\"totalPoms\" : 2"), "Should contain POM count");
        assertTrue(json.contains("\"validPoms\" : 1"), "Should count valid POMs");
        assertTrue(json.contains("\"invalidPoms\" : 1"), "Should count invalid POMs");
        
        // Verify it's valid JSON
        ObjectMapper mapper = new ObjectMapper();
        assertDoesNotThrow(() -> mapper.readTree(json), "Should produce valid JSON");
    }
    
    @Test
    void testJsonFormatterWriteToFile() throws Exception {
        JsonFormatter formatter = new JsonFormatter();
        Path outputFile = tempDir.resolve("report.json");
        
        formatter.write(testResults, outputFile);
        
        assertTrue(Files.exists(outputFile), "Output file should be created");
        String content = Files.readString(outputFile);
        assertTrue(content.contains("POM Validator Tool"), "File should contain formatted JSON");
    }
    
    @Test
    void testMarkdownFormatter() throws Exception {
        MarkdownFormatter formatter = new MarkdownFormatter();
        String markdown = formatter.format(testResults);
        
        assertNotNull(markdown, "Markdown output should not be null");
        assertTrue(markdown.contains("# POM Validation Report"), "Should contain title");
        assertTrue(markdown.contains("## Summary"), "Should contain summary section");
        assertTrue(markdown.contains("| ✅ Valid POMs |"), "Should contain summary table");
        assertTrue(markdown.contains("## Detailed Results"), "Should contain detailed results");
        
        // Check for specific issues
        assertTrue(markdown.contains("Missing groupId"), "Should contain error message");
        assertTrue(markdown.contains("Missing encoding"), "Should contain warning message");
    }
    
    @Test
    void testMarkdownFormatterWriteToFile() throws Exception {
        MarkdownFormatter formatter = new MarkdownFormatter();
        Path outputFile = tempDir.resolve("report.md");
        
        formatter.write(testResults, outputFile);
        
        assertTrue(Files.exists(outputFile), "Output file should be created");
        String content = Files.readString(outputFile);
        assertTrue(content.contains("# POM Validation Report"), "File should contain markdown");
    }
    
    @Test
    void testMarkdownEscaping() {
        MarkdownFormatter formatter = new MarkdownFormatter();
        
        ValidationResult result = new ValidationResult();
        result.addError(ValidationIssue.of(
            "Issue with *special* characters",
            "Fix [brackets] and (parens)"
        ));
        
        Map<Path, ValidationResult> results = new HashMap<>();
        results.put(Path.of("test.xml"), result);
        
        String markdown = formatter.format(results);
        
        // Verify special characters are escaped
        assertTrue(markdown.contains("\\*special\\*"), "Asterisks should be escaped");
        assertTrue(markdown.contains("\\[brackets\\]"), "Brackets should be escaped");
        assertTrue(markdown.contains("\\(parens\\)"), "Parentheses should be escaped");
    }
    
    @Test
    void testEmptyResults() {
        Map<Path, ValidationResult> emptyResults = new HashMap<>();
        
        JsonFormatter jsonFormatter = new JsonFormatter();
        String json = jsonFormatter.format(emptyResults);
        assertTrue(json.contains("\"totalPoms\" : 0"), "Should handle empty results");
        
        MarkdownFormatter mdFormatter = new MarkdownFormatter();
        String markdown = mdFormatter.format(emptyResults);
        assertTrue(markdown.contains("**Total POMs Analyzed:** 0"), "Should handle empty results");
    }
}
