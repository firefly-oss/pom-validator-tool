package com.catalis.tools.pomvalidator.feature;

import com.catalis.tools.pomvalidator.service.PomValidationService;
import com.catalis.tools.pomvalidator.model.ValidationResult;
import com.catalis.tools.pomvalidator.model.ValidationIssue;
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
 * Auto-fix mode for automatically fixing common POM issues.
 * Applies fixes without user interaction for known, safe corrections.
 */
public class AutoFixMode {
    
    private final PomValidationService validationService;
    
    // ANSI color codes
    private static final String RESET = "\u001B[0m";
    private static final String RED = "\u001B[31m";
    private static final String GREEN = "\u001B[32m";
    private static final String YELLOW = "\u001B[33m";
    private static final String BLUE = "\u001B[34m";
    private static final String PURPLE = "\u001B[35m";
    private static final String CYAN = "\u001B[36m";
    private static final String BOLD = "\u001B[1m";
    
    public AutoFixMode() {
        this.validationService = new PomValidationService();
    }
    
    public void runAutoFix(Path pomPath, boolean createBackup) throws Exception {
        System.out.println(BOLD + PURPLE + "🔧 POM Validator - Auto-Fix Mode" + RESET);
        System.out.println(CYAN + "File: " + pomPath.toAbsolutePath() + RESET);
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println();
        
        // Initial validation
        ValidationResult result = validationService.validatePom(pomPath);
        
        if (result.isValid() && result.getWarnings().isEmpty()) {
            System.out.println(GREEN + "✅ This POM is already valid! No fixes needed." + RESET);
            return;
        }
        
        // Create backup if requested
        Path backupPath = null;
        if (createBackup) {
            backupPath = pomPath.resolveSibling(pomPath.getFileName() + ".backup");
            Files.copy(pomPath, backupPath, StandardCopyOption.REPLACE_EXISTING);
            System.out.println(GREEN + "✓ Backup created: " + backupPath.getFileName() + RESET);
            System.out.println();
        }
        
        // Collect fixable issues
        List<ValidationIssue> fixableIssues = new ArrayList<>();
        fixableIssues.addAll(getFixableIssues(result.getErrors()));
        fixableIssues.addAll(getFixableIssues(result.getWarnings()));
        
        if (fixableIssues.isEmpty()) {
            System.out.println(YELLOW + "No automatically fixable issues found." + RESET);
            System.out.println("Some issues require manual intervention.");
            return;
        }
        
        System.out.println(BOLD + "Found " + fixableIssues.size() + " fixable issues:" + RESET);
        System.out.println();
        
        // Apply fixes
        int fixedCount = 0;
        int failedCount = 0;
        
        for (ValidationIssue issue : fixableIssues) {
            System.out.print("  Fixing: " + issue.getMessage() + "... ");
            
            if (applyFix(pomPath, issue)) {
                System.out.println(GREEN + "✓ Fixed" + RESET);
                fixedCount++;
            } else {
                System.out.println(RED + "✗ Failed" + RESET);
                failedCount++;
            }
        }
        
        System.out.println();
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println();
        
        // Re-validate
        System.out.println("Re-validating POM...");
        ValidationResult finalResult = validationService.validatePom(pomPath);
        
        // Summary
        System.out.println();
        System.out.println(BOLD + "Summary:" + RESET);
        System.out.println("  " + GREEN + "Fixed: " + fixedCount + " issues" + RESET);
        if (failedCount > 0) {
            System.out.println("  " + RED + "Failed: " + failedCount + " issues" + RESET);
        }
        System.out.println();
        
        if (finalResult.isValid()) {
            System.out.println(GREEN + BOLD + "✅ POM is now valid!" + RESET);
        } else {
            System.out.println(YELLOW + "⚠ POM still has " + finalResult.getErrors().size() + 
                             " errors and " + finalResult.getWarnings().size() + " warnings." + RESET);
            System.out.println("Some issues require manual intervention.");
        }
        
        if (backupPath != null) {
            System.out.println();
            System.out.println("Backup saved at: " + backupPath.getFileName());
        }
    }
    
    private List<ValidationIssue> getFixableIssues(List<ValidationIssue> issues) {
        List<ValidationIssue> fixable = new ArrayList<>();
        
        for (ValidationIssue issue : issues) {
            if (isFixable(issue)) {
                fixable.add(issue);
            }
        }
        
        return fixable;
    }
    
    private boolean isFixable(ValidationIssue issue) {
        String message = issue.getMessage().toLowerCase();
        
        // List of automatically fixable issues
        return message.contains("groupid is missing") ||
               message.contains("project.build.sourceencoding") ||
               message.contains("project.reporting.outputencoding") ||
               message.contains("maven.compiler.source") ||
               message.contains("maven.compiler.target") ||
               message.contains("duplicate") && message.contains("dependency") ||
               message.contains("test scope") ||
               (message.contains("maven-compiler-plugin") && message.contains("without version"));
    }
    
    private boolean applyFix(Path pomPath, ValidationIssue issue) {
        try {
            String message = issue.getMessage().toLowerCase();
            
            // Missing GAV coordinates
            if (message.contains("groupid is missing")) {
                return addMissingElement(pomPath, "groupId", "com.example");
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
            
            // Test scope
            else if (message.contains("test scope")) {
                return addTestScope(pomPath, issue);
            }
            
            // Plugin version
            else if (message.contains("maven-compiler-plugin") && message.contains("without version")) {
                return addPluginVersion(pomPath, "maven-compiler-plugin", "3.11.0");
            }
            
            return false;
        } catch (Exception e) {
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
        
        // Check if property already exists
        if (model.getProperties().getProperty(property) == null) {
            model.getProperties().setProperty(property, value);
            
            MavenXpp3Writer writer = new MavenXpp3Writer();
            try (FileWriter fileWriter = new FileWriter(pomPath.toFile())) {
                writer.write(fileWriter, model);
            }
            return true;
        }
        
        return false;
    }
    
    private String extractJavaVersion(Path pomPath) throws Exception {
        MavenXpp3Reader reader = new MavenXpp3Reader();
        Model model;
        
        try (FileReader fileReader = new FileReader(pomPath.toFile())) {
            model = reader.read(fileReader);
        }
        
        if (model.getProperties() != null && model.getProperties().getProperty("java.version") != null) {
            return model.getProperties().getProperty("java.version");
        }
        
        return "21";
    }
    
    private boolean removeDuplicateDependency(Path pomPath, ValidationIssue issue) throws Exception {
        MavenXpp3Reader reader = new MavenXpp3Reader();
        Model model;
        
        try (FileReader fileReader = new FileReader(pomPath.toFile())) {
            model = reader.read(fileReader);
        }
        
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
    
    private boolean addTestScope(Path pomPath, ValidationIssue issue) throws Exception {
        MavenXpp3Reader reader = new MavenXpp3Reader();
        Model model;
        
        try (FileReader fileReader = new FileReader(pomPath.toFile())) {
            model = reader.read(fileReader);
        }
        
        String message = issue.getMessage();
        
        // Find junit or other test dependencies
        List<Dependency> dependencies = model.getDependencies();
        if (dependencies != null) {
            for (Dependency dep : dependencies) {
                if ("junit".equals(dep.getGroupId()) && "junit".equals(dep.getArtifactId())) {
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
    
    private boolean addPluginVersion(Path pomPath, String pluginArtifactId, String version) throws Exception {
        MavenXpp3Reader reader = new MavenXpp3Reader();
        Model model;
        
        try (FileReader fileReader = new FileReader(pomPath.toFile())) {
            model = reader.read(fileReader);
        }
        
        if (model.getBuild() != null && model.getBuild().getPlugins() != null) {
            for (Plugin plugin : model.getBuild().getPlugins()) {
                if (pluginArtifactId.equals(plugin.getArtifactId())) {
                    if (plugin.getVersion() == null) {
                        plugin.setVersion(version);
                        
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
