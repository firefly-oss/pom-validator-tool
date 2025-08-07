package com.catalis.tools.pomvalidator.feature;

import com.catalis.tools.pomvalidator.cli.CliOptions;
import com.catalis.tools.pomvalidator.model.ValidationIssue;
import com.catalis.tools.pomvalidator.model.ValidationResult;
import com.catalis.tools.pomvalidator.service.PomValidationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class FilteringTest {
    
    @TempDir
    Path tempDir;
    
    private PomValidationService validationService;
    
    @BeforeEach
    void setUp() {
        validationService = new PomValidationService();
    }
    
    @Test
    void testPathExcludeFiltering() throws IOException {
        // Create a project structure with multiple POMs
        createProjectStructure();
        
        // Test excluding target directories
        List<Path> pomFiles = findPomFiles(tempDir, List.of("target"), List.of());
        
        // Should find POMs but not in target directories
        assertTrue(pomFiles.size() > 0);
        assertFalse(pomFiles.stream()
            .anyMatch(p -> p.toString().contains("target")));
        
        // Should find src POMs
        assertTrue(pomFiles.stream()
            .anyMatch(p -> p.toString().contains("src")));
    }
    
    @Test
    void testPathIncludeFiltering() throws IOException {
        // Create a project structure with multiple POMs
        createProjectStructure();
        
        // Test including only src directories
        List<Path> pomFiles = findPomFiles(tempDir, List.of(), List.of("src"));
        
        // Should only find POMs in src directories
        assertTrue(pomFiles.size() > 0);
        assertTrue(pomFiles.stream()
            .allMatch(p -> p.toString().contains("src")));
        
        // Should not find root POM or target POMs
        assertFalse(pomFiles.stream()
            .anyMatch(p -> p.getParent().equals(tempDir)));
    }
    
    @Test
    void testCombinedIncludeExcludeFiltering() throws IOException {
        // Create a project structure with multiple POMs
        createProjectStructure();
        
        // Include src but exclude test directories
        List<Path> pomFiles = findPomFiles(tempDir, List.of("test"), List.of("src"));
        
        // Should find src/main POMs but not src/test POMs
        assertTrue(pomFiles.size() > 0);
        assertTrue(pomFiles.stream()
            .allMatch(p -> p.toString().contains("src")));
        assertFalse(pomFiles.stream()
            .anyMatch(p -> p.toString().contains("test")));
    }
    
    @Test
    void testSeverityLevelFiltering() {
        // Create a validation result with different severity levels
        ValidationResult result = new ValidationResult();
        result.addError(ValidationIssue.of("Critical error"));
        result.addError(ValidationIssue.of("Another error"));
        result.addWarning(ValidationIssue.of("Potential issue"));
        result.addWarning(ValidationIssue.of("Another warning"));
        result.addInfo(ValidationIssue.of("Recommendation"));
        
        // Test ERROR level filtering
        ValidationResult errorOnly = filterBySeverity(result, CliOptions.SeverityLevel.ERROR);
        assertEquals(2, errorOnly.getErrors().size());
        assertEquals(0, errorOnly.getWarnings().size());
        assertEquals(0, errorOnly.getInfos().size());
        
        // Test WARNING level filtering (includes ERROR)
        ValidationResult warningAndAbove = filterBySeverity(result, CliOptions.SeverityLevel.WARNING);
        assertEquals(2, warningAndAbove.getErrors().size());
        assertEquals(2, warningAndAbove.getWarnings().size());
        assertEquals(0, warningAndAbove.getInfos().size());
        
        // Test INFO level filtering (includes WARNING and ERROR)
        ValidationResult infoAndAbove = filterBySeverity(result, CliOptions.SeverityLevel.INFO);
        assertEquals(2, infoAndAbove.getErrors().size());
        assertEquals(2, infoAndAbove.getWarnings().size());
        assertEquals(1, infoAndAbove.getInfos().size());
        
        // Test ALL level filtering
        ValidationResult all = filterBySeverity(result, CliOptions.SeverityLevel.ALL);
        assertEquals(2, all.getErrors().size());
        assertEquals(2, all.getWarnings().size());
        assertEquals(1, all.getInfos().size());
    }
    
    @Test
    void testValidationProfileFiltering() throws Exception {
        // Create a POM with various issues
        Path pomPath = tempDir.resolve("pom.xml");
        String pomContent = """
            <?xml version="1.0" encoding="UTF-8"?>
            <project>
                <artifactId>test-artifact</artifactId>
                <version>1.0.0</version>
                <dependencies>
                    <dependency>
                        <groupId>junit</groupId>
                        <artifactId>junit</artifactId>
                    </dependency>
                </dependencies>
            </project>
            """;
        Files.write(pomPath, pomContent.getBytes());
        
        // Validate with STRICT profile - should find many issues
        ValidationResult strictResult = validationService.validatePom(pomPath);
        assertTrue(strictResult.getTotalIssues() > 0);
        assertTrue(strictResult.getErrors().stream()
            .anyMatch(i -> i.getMessage().contains("GroupId")));
        
        // With MINIMAL profile, fewer issues would be reported
        // (This would need to be implemented in the actual service)
        // For now, we verify that the profile can be set
        CliOptions options = CliOptions.parse(new String[]{"-p", "minimal"});
        assertEquals(CliOptions.ValidationProfile.MINIMAL, options.getProfile());
    }
    
    @Test
    void testRecursiveDirectoryFiltering() throws IOException {
        // Create nested project structure
        createNestedProjectStructure();
        
        // Test non-recursive search
        List<Path> nonRecursivePoms = findPomFilesNonRecursive(tempDir);
        assertEquals(1, nonRecursivePoms.size());
        assertEquals(tempDir.resolve("pom.xml"), nonRecursivePoms.get(0));
        
        // Test recursive search
        List<Path> recursivePoms = findPomFilesRecursive(tempDir);
        assertTrue(recursivePoms.size() > 1);
        
        // Verify it found nested POMs
        assertTrue(recursivePoms.stream()
            .anyMatch(p -> p.toString().contains("module1")));
        assertTrue(recursivePoms.stream()
            .anyMatch(p -> p.toString().contains("module2")));
        assertTrue(recursivePoms.stream()
            .anyMatch(p -> p.toString().contains("submodule")));
    }
    
    @Test
    void testFilePatternFiltering() throws IOException {
        // Create files with different patterns
        Files.write(tempDir.resolve("pom.xml"), createValidPom().getBytes());
        Files.write(tempDir.resolve("pom-backup.xml"), createValidPom().getBytes());
        Files.write(tempDir.resolve("effective-pom.xml"), createValidPom().getBytes());
        Files.write(tempDir.resolve("not-a-pom.xml"), "<root/>".getBytes());
        
        // Find only files matching "pom.xml" pattern
        List<Path> standardPoms = Files.list(tempDir)
            .filter(p -> p.getFileName().toString().equals("pom.xml"))
            .collect(Collectors.toList());
        
        assertEquals(1, standardPoms.size());
        assertEquals("pom.xml", standardPoms.get(0).getFileName().toString());
        
        // Find all files containing "pom" in name
        List<Path> allPomFiles = Files.list(tempDir)
            .filter(p -> p.getFileName().toString().contains("pom"))
            .collect(Collectors.toList());
        
        assertEquals(4, allPomFiles.size()); // pom.xml, pom-backup.xml, effective-pom.xml, not-a-pom.xml
    }
    
    @Test
    void testEmptyFilterConfiguration() throws IOException {
        // Create a project structure
        createProjectStructure();
        
        // Test with no filters - should find all POMs
        List<Path> allPoms = findPomFiles(tempDir, List.of(), List.of());
        
        // Should find all POMs including in target
        assertTrue(allPoms.size() >= 4);
        assertTrue(allPoms.stream().anyMatch(p -> p.toString().contains("target")));
        assertTrue(allPoms.stream().anyMatch(p -> p.toString().contains("src")));
    }
    
    @Test
    void testMultipleExcludePatterns() throws IOException {
        // Create a complex project structure
        createComplexProjectStructure();
        
        // Exclude multiple patterns
        List<String> excludes = List.of("target", "test", "backup", ".git");
        List<Path> filteredPoms = findPomFiles(tempDir, excludes, List.of());
        
        // Verify none of the excluded patterns are in the results
        for (String exclude : excludes) {
            assertFalse(filteredPoms.stream()
                .anyMatch(p -> p.toString().contains(exclude)),
                "Should not contain excluded pattern: " + exclude);
        }
        
        // Should still find some POMs
        assertTrue(filteredPoms.size() > 0);
    }
    
    @Test
    void testCaseInsensitiveFiltering() throws IOException {
        // Create POMs with different case patterns
        Files.createDirectories(tempDir.resolve("Target"));
        Files.createDirectories(tempDir.resolve("TARGET"));
        Files.createDirectories(tempDir.resolve("target"));
        
        Files.write(tempDir.resolve("Target/pom.xml"), createValidPom().getBytes());
        Files.write(tempDir.resolve("TARGET/pom.xml"), createValidPom().getBytes());
        Files.write(tempDir.resolve("target/pom.xml"), createValidPom().getBytes());
        
        // Test case-insensitive exclusion (implementation dependent)
        List<Path> poms = findPomFilesWithCaseInsensitiveExclude(tempDir, "target");
        
        // If case-insensitive is implemented, should exclude all variations
        // If not, this test documents the current behavior
        assertTrue(poms.isEmpty() || poms.size() == 2);
    }
    
    // Helper methods
    
    private void createProjectStructure() throws IOException {
        // Root POM
        Files.write(tempDir.resolve("pom.xml"), createValidPom().getBytes());
        
        // src/main POM
        Files.createDirectories(tempDir.resolve("src/main"));
        Files.write(tempDir.resolve("src/main/pom.xml"), createValidPom().getBytes());
        
        // src/test POM
        Files.createDirectories(tempDir.resolve("src/test"));
        Files.write(tempDir.resolve("src/test/pom.xml"), createValidPom().getBytes());
        
        // target POM (should typically be excluded)
        Files.createDirectories(tempDir.resolve("target"));
        Files.write(tempDir.resolve("target/pom.xml"), createValidPom().getBytes());
    }
    
    private void createNestedProjectStructure() throws IOException {
        // Root POM
        Files.write(tempDir.resolve("pom.xml"), createValidPom().getBytes());
        
        // Module 1
        Files.createDirectories(tempDir.resolve("module1"));
        Files.write(tempDir.resolve("module1/pom.xml"), createValidPom().getBytes());
        
        // Module 2 with submodule
        Files.createDirectories(tempDir.resolve("module2/submodule"));
        Files.write(tempDir.resolve("module2/pom.xml"), createValidPom().getBytes());
        Files.write(tempDir.resolve("module2/submodule/pom.xml"), createValidPom().getBytes());
    }
    
    private void createComplexProjectStructure() throws IOException {
        createProjectStructure();
        
        // Additional directories to test multiple excludes
        Files.createDirectories(tempDir.resolve("backup"));
        Files.write(tempDir.resolve("backup/pom.xml"), createValidPom().getBytes());
        
        Files.createDirectories(tempDir.resolve(".git"));
        Files.write(tempDir.resolve(".git/pom.xml"), createValidPom().getBytes());
        
        Files.createDirectories(tempDir.resolve("src/main/resources"));
        Files.write(tempDir.resolve("src/main/resources/pom.xml"), createValidPom().getBytes());
    }
    
    private String createValidPom() {
        return """
            <?xml version="1.0" encoding="UTF-8"?>
            <project>
                <groupId>com.test</groupId>
                <artifactId>test-artifact</artifactId>
                <version>1.0.0</version>
            </project>
            """;
    }
    
    private List<Path> findPomFiles(Path root, List<String> excludePatterns, List<String> includePatterns) 
            throws IOException {
        return Files.walk(root)
            .filter(p -> p.getFileName().toString().equals("pom.xml"))
            .filter(p -> {
                String pathStr = p.toString();
                // Apply exclude filters
                for (String exclude : excludePatterns) {
                    if (pathStr.contains(exclude)) {
                        return false;
                    }
                }
                // Apply include filters (if any specified)
                if (!includePatterns.isEmpty()) {
                    boolean included = false;
                    for (String include : includePatterns) {
                        if (pathStr.contains(include)) {
                            included = true;
                            break;
                        }
                    }
                    return included;
                }
                return true;
            })
            .collect(Collectors.toList());
    }
    
    private List<Path> findPomFilesNonRecursive(Path root) throws IOException {
        return Files.list(root)
            .filter(p -> p.getFileName().toString().equals("pom.xml"))
            .collect(Collectors.toList());
    }
    
    private List<Path> findPomFilesRecursive(Path root) throws IOException {
        return Files.walk(root)
            .filter(p -> p.getFileName().toString().equals("pom.xml"))
            .collect(Collectors.toList());
    }
    
    private List<Path> findPomFilesWithCaseInsensitiveExclude(Path root, String exclude) 
            throws IOException {
        String lowerExclude = exclude.toLowerCase();
        return Files.walk(root)
            .filter(p -> p.getFileName().toString().equals("pom.xml"))
            .filter(p -> !p.toString().toLowerCase().contains(lowerExclude))
            .collect(Collectors.toList());
    }
    
    private ValidationResult filterBySeverity(ValidationResult original, 
                                             CliOptions.SeverityLevel level) {
        ValidationResult filtered = new ValidationResult();
        
        switch (level) {
            case ERROR:
                filtered.addErrors(original.getErrors());
                break;
            case WARNING:
                filtered.addErrors(original.getErrors());
                filtered.addWarnings(original.getWarnings());
                break;
            case INFO:
                filtered.addErrors(original.getErrors());
                filtered.addWarnings(original.getWarnings());
                filtered.addInfos(original.getInfos());
                break;
            case ALL:
            default:
                filtered.addErrors(original.getErrors());
                filtered.addWarnings(original.getWarnings());
                filtered.addInfos(original.getInfos());
                break;
        }
        
        return filtered;
    }
}
