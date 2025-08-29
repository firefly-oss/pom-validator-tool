package com.firefly.tools.pomvalidator.feature;

import com.firefly.tools.pomvalidator.service.PomValidationService;
import com.firefly.tools.pomvalidator.model.ValidationResult;
import com.firefly.tools.pomvalidator.model.ValidationIssue;
import org.apache.maven.model.Model;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;

/**
 * Interactive mode for reviewing and fixing validation issues.
 * Provides a user-friendly interface for addressing POM problems.
 */
public class InteractiveMode {
    
    private final PomValidationService validationService;
    private final Scanner scanner;
    
    // ANSI color codes
    private static final String RESET = "\u001B[0m";
    private static final String RED = "\u001B[31m";
    private static final String GREEN = "\u001B[32m";
    private static final String YELLOW = "\u001B[33m";
    private static final String BLUE = "\u001B[34m";
    private static final String PURPLE = "\u001B[35m";
    private static final String CYAN = "\u001B[36m";
    private static final String BOLD = "\u001B[1m";
    
    public InteractiveMode() {
        this.validationService = new PomValidationService();
        this.scanner = new Scanner(System.in);
    }
    
    public void runInteractive(Path pomPath) throws Exception {
        clearScreen();
        System.out.println(BOLD + PURPLE + "🔧 POM Validator - Interactive Mode" + RESET);
        System.out.println(CYAN + "File: " + pomPath.toAbsolutePath() + RESET);
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println();
        
        // Validate POM
        ValidationResult result = validationService.validatePom(pomPath);
        
        if (result.isValid() && result.getWarnings().isEmpty() && result.getInfos().isEmpty()) {
            System.out.println(GREEN + "✅ This POM is perfectly valid! No issues found." + RESET);
            return;
        }
        
        // Show summary
        System.out.println(BOLD + "Validation Summary:" + RESET);
        System.out.println("  " + RED + "Errors: " + result.getErrors().size() + RESET);
        System.out.println("  " + YELLOW + "Warnings: " + result.getWarnings().size() + RESET);
        System.out.println("  " + CYAN + "Info: " + result.getInfos().size() + RESET);
        System.out.println();
        
        // Process issues interactively
        List<ValidationIssue> allIssues = new ArrayList<>();
        allIssues.addAll(result.getErrors());
        allIssues.addAll(result.getWarnings());
        allIssues.addAll(result.getInfos());
        
        if (allIssues.isEmpty()) {
            return;
        }
        
        System.out.println(YELLOW + "Would you like to review and fix these issues? (y/n): " + RESET);
        String response = scanner.nextLine().trim().toLowerCase();
        
        if (!response.equals("y") && !response.equals("yes")) {
            System.out.println("Exiting interactive mode.");
            return;
        }
        
        // Create backup
        Path backupPath = pomPath.resolveSibling(pomPath.getFileName() + ".backup");
        Files.copy(pomPath, backupPath, StandardCopyOption.REPLACE_EXISTING);
        System.out.println(GREEN + "✓ Backup created: " + backupPath.getFileName() + RESET);
        System.out.println();
        
        // Process each issue
        int fixedCount = 0;
        int skippedCount = 0;
        
        for (int i = 0; i < allIssues.size(); i++) {
            ValidationIssue issue = allIssues.get(i);
            String issueType = getIssueType(issue, result);
            
            clearScreen();
            System.out.println(BOLD + "Issue " + (i + 1) + " of " + allIssues.size() + RESET);
            System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            System.out.println();
            
            // Display issue
            String color = issueType.equals("ERROR") ? RED : 
                          issueType.equals("WARNING") ? YELLOW : CYAN;
            System.out.println(color + BOLD + issueType + ": " + RESET + issue.getMessage());
            
            if (issue.hasSuggestion()) {
                System.out.println(GREEN + "💡 Suggestion: " + issue.getSuggestion() + RESET);
            }
            
            System.out.println();
            System.out.println("Options:");
            System.out.println("  " + GREEN + "[f]" + RESET + " - Apply suggested fix (if available)");
            System.out.println("  " + YELLOW + "[s]" + RESET + " - Skip this issue");
            System.out.println("  " + BLUE + "[v]" + RESET + " - View POM section");
            System.out.println("  " + CYAN + "[e]" + RESET + " - Edit manually");
            System.out.println("  " + RED + "[q]" + RESET + " - Quit");
            System.out.println();
            System.out.print("Your choice: ");
            
            String choice = scanner.nextLine().trim().toLowerCase();
            
            switch (choice) {
                case "f":
                    if (issue.hasSuggestion() && applyFix(pomPath, issue)) {
                        System.out.println(GREEN + "✓ Fix applied!" + RESET);
                        fixedCount++;
                    } else {
                        System.out.println(YELLOW + "⚠ Automatic fix not available for this issue." + RESET);
                    }
                    Thread.sleep(1000);
                    break;
                    
                case "s":
                    System.out.println(YELLOW + "Skipped." + RESET);
                    skippedCount++;
                    Thread.sleep(500);
                    break;
                    
                case "v":
                    viewPomSection(pomPath, issue);
                    System.out.println("\nPress Enter to continue...");
                    scanner.nextLine();
                    i--; // Repeat current issue
                    break;
                    
                case "e":
                    openInEditor(pomPath);
                    System.out.println("\nPress Enter when done editing...");
                    scanner.nextLine();
                    // Re-validate after edit
                    result = validationService.validatePom(pomPath);
                    allIssues.clear();
                    allIssues.addAll(result.getErrors());
                    allIssues.addAll(result.getWarnings());
                    allIssues.addAll(result.getInfos());
                    i = -1; // Start over
                    break;
                    
                case "q":
                    System.out.println(RED + "Exiting interactive mode." + RESET);
                    return;
                    
                default:
                    System.out.println(RED + "Invalid choice. Please try again." + RESET);
                    Thread.sleep(1000);
                    i--; // Repeat current issue
            }
        }
        
        // Final summary
        clearScreen();
        System.out.println(BOLD + GREEN + "✅ Interactive Session Complete!" + RESET);
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println();
        System.out.println("Summary:");
        System.out.println("  " + GREEN + "Fixed: " + fixedCount + " issues" + RESET);
        System.out.println("  " + YELLOW + "Skipped: " + skippedCount + " issues" + RESET);
        System.out.println();
        
        // Re-validate
        System.out.println("Re-validating POM...");
        ValidationResult finalResult = validationService.validatePom(pomPath);
        
        if (finalResult.isValid()) {
            System.out.println(GREEN + BOLD + "✅ POM is now valid!" + RESET);
        } else {
            System.out.println(YELLOW + "⚠ POM still has " + finalResult.getErrors().size() + 
                             " errors and " + finalResult.getWarnings().size() + " warnings." + RESET);
        }
        
        System.out.println();
        System.out.println("Backup saved at: " + backupPath.getFileName());
        
        // Ask to remove backup
        System.out.print("\nRemove backup file? (y/n): ");
        String removeBackup = scanner.nextLine().trim().toLowerCase();
        if (removeBackup.equals("y") || removeBackup.equals("yes")) {
            Files.delete(backupPath);
            System.out.println(GREEN + "✓ Backup removed." + RESET);
        }
    }
    
    private String getIssueType(ValidationIssue issue, ValidationResult result) {
        if (result.getErrors().contains(issue)) return "ERROR";
        if (result.getWarnings().contains(issue)) return "WARNING";
        return "INFO";
    }
    
    private boolean applyFix(Path pomPath, ValidationIssue issue) {
        try {
            String message = issue.getMessage().toLowerCase();
            String suggestion = issue.hasSuggestion() ? issue.getSuggestion().toLowerCase() : "";
            
            // Missing GAV coordinates
            if (message.contains("groupid is missing")) {
                return addMissingElement(pomPath, "groupId", "com.example");
            } else if (message.contains("missing artifactid")) {
                return addMissingElement(pomPath, "artifactId", "my-artifact");
            } else if (message.contains("missing version")) {
                return addMissingElement(pomPath, "version", "1.0.0-SNAPSHOT");
            }
            
            // Properties
            else if (message.contains("project.build.sourceencoding")) {
                return setProperty(pomPath, "project.build.sourceEncoding", "UTF-8");
            } else if (message.contains("project.reporting.outputencoding")) {
                return setProperty(pomPath, "project.reporting.outputEncoding", "UTF-8");
            } else if (message.contains("maven.compiler.source")) {
                String javaVersion = extractJavaVersion(pomPath);
                return setProperty(pomPath, "maven.compiler.source", javaVersion);
            } else if (message.contains("maven.compiler.target")) {
                String javaVersion = extractJavaVersion(pomPath);
                return setProperty(pomPath, "maven.compiler.target", javaVersion);
            }
            
            // Duplicate dependencies
            else if (message.contains("duplicate") && message.contains("dependency")) {
                return removeDuplicateDependency(pomPath, issue);
            }
            
            // Missing dependency version
            else if (message.contains("dependency") && message.contains("version")) {
                return fixDependencyVersion(pomPath, issue);
            }
            
            // Test scope
            else if (message.contains("test scope")) {
                return addTestScope(pomPath, issue);
            }
            
            // Plugin version
            else if (message.contains("plugin") && message.contains("without version")) {
                return addPluginVersion(pomPath, issue);
            }
            
            return false;
        } catch (Exception e) {
            System.err.println(RED + "Error applying fix: " + e.getMessage() + RESET);
            return false;
        }
    }
    
    private boolean addMissingElement(Path pomPath, String element, String defaultValue) throws Exception {
        MavenXpp3Reader reader = new MavenXpp3Reader();
        Model model;
        
        try (FileReader fileReader = new FileReader(pomPath.toFile())) {
            model = reader.read(fileReader);
        }
        
        boolean modified = false;
        switch (element) {
            case "groupId":
                if (model.getGroupId() == null) {
                    model.setGroupId(defaultValue);
                    modified = true;
                }
                break;
            case "artifactId":
                if (model.getArtifactId() == null) {
                    model.setArtifactId(defaultValue);
                    modified = true;
                }
                break;
            case "version":
                if (model.getVersion() == null) {
                    model.setVersion(defaultValue);
                    modified = true;
                }
                break;
        }
        
        if (modified) {
            MavenXpp3Writer writer = new MavenXpp3Writer();
            try (FileWriter fileWriter = new FileWriter(pomPath.toFile())) {
                writer.write(fileWriter, model);
            }
        }
        
        return modified;
    }
    
    private boolean setProperty(Path pomPath, String property, String value) throws Exception {
        MavenXpp3Reader reader = new MavenXpp3Reader();
        Model model;
        
        try (FileReader fileReader = new FileReader(pomPath.toFile())) {
            model = reader.read(fileReader);
        }
        
        if (model.getProperties() == null) {
            model.setProperties(new Properties());
        }
        model.getProperties().setProperty(property, value);
        
        MavenXpp3Writer writer = new MavenXpp3Writer();
        try (FileWriter fileWriter = new FileWriter(pomPath.toFile())) {
            writer.write(fileWriter, model);
        }
        
        return true;
    }
    
    private void viewPomSection(Path pomPath, ValidationIssue issue) {
        try {
            List<String> lines = Files.readAllLines(pomPath);
            String message = issue.getMessage().toLowerCase();
            
            // Find relevant section based on issue
            int startLine = 0;
            int endLine = Math.min(lines.size(), 30);
            
            if (message.contains("dependency") || message.contains("dependencies")) {
                startLine = findLineContaining(lines, "<dependencies>");
                endLine = Math.min(findLineContaining(lines, "</dependencies>") + 1, lines.size());
            } else if (message.contains("plugin")) {
                startLine = findLineContaining(lines, "<plugins>");
                endLine = Math.min(findLineContaining(lines, "</plugins>") + 1, lines.size());
            }
            
            System.out.println("\n" + CYAN + "POM Section:" + RESET);
            System.out.println("─────────────────────────────────────────");
            
            for (int i = Math.max(0, startLine); i < Math.min(endLine, lines.size()); i++) {
                System.out.printf("%4d: %s\n", i + 1, lines.get(i));
            }
            
        } catch (IOException e) {
            System.err.println(RED + "Error reading POM: " + e.getMessage() + RESET);
        }
    }
    
    private int findLineContaining(List<String> lines, String text) {
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).contains(text)) {
                return i;
            }
        }
        return 0;
    }
    
    private void openInEditor(Path pomPath) {
        try {
            String editor = System.getenv("EDITOR");
            if (editor == null) {
                editor = "vi"; // Default to vi
            }
            
            ProcessBuilder pb = new ProcessBuilder(editor, pomPath.toString());
            pb.inheritIO();
            Process p = pb.start();
            p.waitFor();
        } catch (Exception e) {
            System.err.println(YELLOW + "Could not open editor. Please edit the file manually." + RESET);
            System.err.println("File: " + pomPath.toAbsolutePath());
        }
    }
    
    private void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }
    
    // Additional helper methods for auto-fix functionality
    
    private String extractJavaVersion(Path pomPath) throws Exception {
        MavenXpp3Reader reader = new MavenXpp3Reader();
        Model model;
        
        try (FileReader fileReader = new FileReader(pomPath.toFile())) {
            model = reader.read(fileReader);
        }
        
        // Check if java.version property exists
        if (model.getProperties() != null && model.getProperties().getProperty("java.version") != null) {
            return model.getProperties().getProperty("java.version");
        }
        
        // Default to Java 21
        return "21";
    }
    
    private boolean removeDuplicateDependency(Path pomPath, ValidationIssue issue) throws Exception {
        MavenXpp3Reader reader = new MavenXpp3Reader();
        Model model;
        
        try (FileReader fileReader = new FileReader(pomPath.toFile())) {
            model = reader.read(fileReader);
        }
        
        // Extract dependency info from the issue message
        String message = issue.getMessage();
        String[] parts = message.split(":");
        if (parts.length < 2) return false;
        
        String artifactInfo = parts[1].trim();
        String[] gavParts = artifactInfo.split(":");
        if (gavParts.length < 2) return false;
        
        String groupId = gavParts[0];
        String artifactId = gavParts[1];
        
        // Find and remove duplicates, keeping the first one
        List<Dependency> dependencies = model.getDependencies();
        if (dependencies != null) {
            Set<String> seen = new HashSet<>();
            List<Dependency> filtered = new ArrayList<>();
            
            for (Dependency dep : dependencies) {
                String key = dep.getGroupId() + ":" + dep.getArtifactId();
                if (!seen.contains(key)) {
                    seen.add(key);
                    filtered.add(dep);
                }
            }
            
            if (filtered.size() < dependencies.size()) {
                model.setDependencies(filtered);
                
                MavenXpp3Writer writer = new MavenXpp3Writer();
                try (FileWriter fileWriter = new FileWriter(pomPath.toFile())) {
                    writer.write(fileWriter, model);
                }
                return true;
            }
        }
        
        return false;
    }
    
    private boolean fixDependencyVersion(Path pomPath, ValidationIssue issue) throws Exception {
        // This would need more sophisticated parsing to identify the specific dependency
        // For now, return false to indicate manual fix is needed
        return false;
    }
    
    private boolean addTestScope(Path pomPath, ValidationIssue issue) throws Exception {
        MavenXpp3Reader reader = new MavenXpp3Reader();
        Model model;
        
        try (FileReader fileReader = new FileReader(pomPath.toFile())) {
            model = reader.read(fileReader);
        }
        
        // Extract dependency name from issue
        String message = issue.getMessage();
        String[] parts = message.split(":");
        if (parts.length < 3) return false;
        
        String groupId = parts[1].trim();
        String artifactId = parts[2].trim();
        
        // Find the dependency and add test scope
        List<Dependency> dependencies = model.getDependencies();
        if (dependencies != null) {
            for (Dependency dep : dependencies) {
                if (groupId.equals(dep.getGroupId()) && artifactId.equals(dep.getArtifactId())) {
                    if (dep.getScope() == null || !dep.getScope().equals("test")) {
                        dep.setScope("test");
                        
                        MavenXpp3Writer writer = new MavenXpp3Writer();
                        try (FileWriter fileWriter = new FileWriter(pomPath.toFile())) {
                            writer.write(fileWriter, model);
                        }
                        return true;
                    }
                }
            }
        }
        
        return false;
    }
    
    private boolean addPluginVersion(Path pomPath, ValidationIssue issue) throws Exception {
        MavenXpp3Reader reader = new MavenXpp3Reader();
        Model model;
        
        try (FileReader fileReader = new FileReader(pomPath.toFile())) {
            model = reader.read(fileReader);
        }
        
        // Extract plugin name from issue
        String message = issue.getMessage();
        if (!message.contains("maven-compiler-plugin")) {
            return false; // Only handle compiler plugin for now
        }
        
        // Add version to maven-compiler-plugin
        if (model.getBuild() != null && model.getBuild().getPlugins() != null) {
            for (Plugin plugin : model.getBuild().getPlugins()) {
                if ("org.apache.maven.plugins".equals(plugin.getGroupId()) && 
                    "maven-compiler-plugin".equals(plugin.getArtifactId())) {
                    if (plugin.getVersion() == null) {
                        plugin.setVersion("3.11.0");
                        
                        MavenXpp3Writer writer = new MavenXpp3Writer();
                        try (FileWriter fileWriter = new FileWriter(pomPath.toFile())) {
                            writer.write(fileWriter, model);
                        }
                        return true;
                    }
                }
            }
        }
        
        return false;
    }
}
