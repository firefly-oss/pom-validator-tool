package com.catalis.tools.pomvalidator;

import com.catalis.tools.pomvalidator.cli.CliOptions;
import com.catalis.tools.pomvalidator.cli.ui.*;
import com.catalis.tools.pomvalidator.feature.OutputFormatter;
import com.catalis.tools.pomvalidator.feature.WatchMode;
import com.catalis.tools.pomvalidator.feature.InteractiveMode;
import com.catalis.tools.pomvalidator.feature.AutoFixMode;
import com.catalis.tools.pomvalidator.feature.formatter.JsonFormatter;
import com.catalis.tools.pomvalidator.feature.formatter.MarkdownFormatter;
import com.catalis.tools.pomvalidator.service.PomValidationService;
import com.catalis.tools.pomvalidator.model.ValidationResult;
import com.catalis.tools.pomvalidator.model.ValidationIssue;
import org.apache.maven.model.Model;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Main application class for the POM Validator Tool.
 * This tool validates Maven POM files for structure, dependencies, and common issues.
 * Supports both single POM files and multi-module Maven projects.
 */
public class PomValidatorApplication {
    
    private static final String VERSION = "1.0.0-SNAPSHOT";
    private static final String TOOL_NAME = "POM Validator Tool";
    
    // ANSI color codes for terminal output
    private static String RESET = "\u001B[0m";
    private static String RED = "\u001B[31m";
    private static String GREEN = "\u001B[32m";
    private static String YELLOW = "\u001B[33m";
    private static String BLUE = "\u001B[34m";
    private static String PURPLE = "\u001B[35m";
    private static String CYAN = "\u001B[36m";
    private static String BOLD = "\u001B[1m";

    public static void main(String[] args) {
        PomValidatorApplication app = new PomValidatorApplication();
        
        // Parse CLI options
        CliOptions options = CliOptions.parse(args);
        
        // Handle help and version
        if (options.isHelp()) {
            new HelpScreen().show();
            System.exit(0);
        }
        
        if (options.isVersion()) {
            CliUI ui = new CliUI();
            ui.printHeader("VERSION INFO", '═');
            ui.println(ui.bold(ui.cyan("📦 POM Validator Tool")) + " " + ui.green("v1.0.0"));
            ui.println(ui.gray("Build: 2024.01.release"));
            ui.println(ui.gray("Java: " + System.getProperty("java.version")));
            ui.newLine();
            ui.println(ui.dim("Part of Firefly OpenCore Banking Platform"));
            ui.println(ui.dim("© 2024 Firefly OpenCore. Apache License 2.0"));
            ui.printDivider('═');
            System.exit(0);
        }
        
        // Handle color output
        if (options.isNoColor()) {
            app.disableColors();
        }
        
        try {
            // Handle different modes
            if (options.isWatch()) {
                app.runWatchMode(options);
            } else if (options.isInteractive()) {
                app.runInteractiveMode(options);
            } else if (options.isAutoFix()) {
                app.runAutoFixMode(options);
            } else {
                app.runValidation(options);
            }
        } catch (Exception e) {
            if (!options.isQuiet()) {
                System.err.println(RED + "❌ Error: " + e.getMessage() + RESET);
            }
            if (options.isVerbose() || System.getProperty("debug") != null) {
                e.printStackTrace();
            }
            System.exit(1);
        }
    }
    
    private void disableColors() {
        // Disable ANSI colors
        RESET = "";
        RED = "";
        GREEN = "";
        YELLOW = "";
        BLUE = "";
        PURPLE = "";
        CYAN = "";
        BOLD = "";
    }
    
    private void runWatchMode(CliOptions options) throws Exception {
        WatchMode watchMode = new WatchMode();
        watchMode.watch(options.getTargetPath(), options.isRecursive());
    }
    
    private void runInteractiveMode(CliOptions options) throws Exception {
        Path targetPath = options.getTargetPath();
        
        // If directory, look for pom.xml
        if (Files.isDirectory(targetPath)) {
            targetPath = targetPath.resolve("pom.xml");
        }
        
        if (!Files.exists(targetPath)) {
            throw new IllegalArgumentException("POM file not found: " + targetPath);
        }
        
        InteractiveMode interactive = new InteractiveMode();
        interactive.runInteractive(targetPath);
    }
    
    private void runAutoFixMode(CliOptions options) throws Exception {
        Path targetPath = options.getTargetPath();
        
        // If directory, look for pom.xml
        if (Files.isDirectory(targetPath)) {
            targetPath = targetPath.resolve("pom.xml");
        }
        
        if (!Files.exists(targetPath)) {
            throw new IllegalArgumentException("POM file not found: " + targetPath);
        }
        
        AutoFixMode autoFix = new AutoFixMode();
        autoFix.runAutoFix(targetPath, true); // Always create backup in auto-fix mode
    }
    
    private void runValidation(CliOptions options) throws Exception {
        Path targetPath = options.getTargetPath();
        
        // Determine what to validate
        List<Path> pomsToValidate = findPomsToValidate(targetPath, options);
        
        // Validate all POMs
        Map<Path, ValidationResult> results = validatePoms(pomsToValidate, options);
        
        // Filter results based on severity
        results = filterBySeverity(results, options.getSeverityLevel());
        
        // Output results
        outputResults(results, options);
        
        // Determine exit code
        boolean hasErrors = results.values().stream()
            .anyMatch(r -> !r.getErrors().isEmpty());
        System.exit(hasErrors ? 1 : 0);
    }
    
    private List<Path> findPomsToValidate(Path targetPath, CliOptions options) throws IOException {
        List<Path> pomsToValidate = new ArrayList<>();
        
        if (Files.isRegularFile(targetPath)) {
            if (!targetPath.getFileName().toString().equals("pom.xml")) {
                throw new IllegalArgumentException("File is not a pom.xml: " + targetPath);
            }
            pomsToValidate.add(targetPath);
        } else if (Files.isDirectory(targetPath)) {
            Path directPom = targetPath.resolve("pom.xml");
            
            if (options.isRecursive()) {
                // Find all pom.xml files recursively
                pomsToValidate = findAllPoms(targetPath, options);
                if (pomsToValidate.isEmpty()) {
                    throw new IllegalArgumentException("No pom.xml files found in: " + targetPath);
                }
            } else {
                // Just validate the pom.xml in the directory
                if (!Files.exists(directPom)) {
                    throw new IllegalArgumentException("No pom.xml found in directory: " + targetPath);
                }
                pomsToValidate.add(directPom);
            }
        } else {
            throw new IllegalArgumentException("Path does not exist: " + targetPath);
        }
        
        // Sort POMs to validate parent first if it's a multi-module project
        return sortPomsParentFirst(pomsToValidate);
    }
    
    private Map<Path, ValidationResult> validatePoms(List<Path> poms, CliOptions options) throws Exception {
        PomValidationService validationService = new PomValidationService();
        Map<Path, ValidationResult> results = new LinkedHashMap<>();
        
        if (!options.isQuiet() && poms.size() > 1) {
            System.out.println();
            System.out.println(BOLD + PURPLE + "🔍 Validating " + poms.size() + " POM files..." + RESET);
            System.out.println();
        }
        
        for (Path pomPath : poms) {
            ValidationResult result = validationService.validatePom(pomPath);
            results.put(pomPath, result);
            
            if (options.isFailFast() && !result.getErrors().isEmpty()) {
                if (!options.isQuiet()) {
                    System.out.println(RED + "❌ Validation failed (fail-fast mode)" + RESET);
                }
                break;
            }
        }
        
        return results;
    }
    
    private Map<Path, ValidationResult> filterBySeverity(Map<Path, ValidationResult> results, 
                                                         CliOptions.SeverityLevel level) {
        if (level == CliOptions.SeverityLevel.ALL) {
            return results;
        }
        
        Map<Path, ValidationResult> filtered = new LinkedHashMap<>();
        
        for (Map.Entry<Path, ValidationResult> entry : results.entrySet()) {
            ValidationResult result = entry.getValue();
            ValidationResult filteredResult = new ValidationResult();
            
            // Always include errors
            filteredResult.addErrors(result.getErrors());
            
            // Include warnings if level allows
            if (level.includes(CliOptions.SeverityLevel.WARNING)) {
                filteredResult.addWarnings(result.getWarnings());
            }
            
            // Include info if level allows
            if (level.includes(CliOptions.SeverityLevel.INFO)) {
                filteredResult.addInfos(result.getInfos());
            }
            
            filtered.put(entry.getKey(), filteredResult);
        }
        
        return filtered;
    }
    
    private void outputResults(Map<Path, ValidationResult> results, CliOptions options) throws IOException {
        // Get the appropriate formatter
        OutputFormatter formatter = null;
        
        switch (options.getOutputFormat()) {
            case JSON:
                formatter = new JsonFormatter();
                break;
            case MARKDOWN:
                formatter = new MarkdownFormatter();
                break;
            case CONSOLE:
            default:
                // Use console output
                if (!options.isQuiet()) {
                    outputConsoleResults(results, options);
                }
                return;
        }
        
        // Format and output
        if (formatter != null) {
            if (options.getOutputFile() != null) {
                formatter.write(results, options.getOutputFile());
                if (!options.isQuiet()) {
                    System.out.println(GREEN + "✓ Report written to: " + options.getOutputFile() + RESET);
                }
            } else {
                System.out.println(formatter.format(results));
            }
        }
    }
    
    private void outputConsoleResults(Map<Path, ValidationResult> results, CliOptions options) {
        Path basePath = options.getTargetPath();
        
        int totalErrors = 0;
        int totalWarnings = 0;
        int totalInfos = 0;
        
        for (ValidationResult result : results.values()) {
            totalErrors += result.getErrors().size();
            totalWarnings += result.getWarnings().size();
            totalInfos += result.getInfos().size();
        }
        
        // Output detailed results unless summary-only
        if (!options.isSummaryOnly()) {
            for (Map.Entry<Path, ValidationResult> entry : results.entrySet()) {
                printValidationResult(entry.getKey(), entry.getValue(), basePath);
            }
        }
        
        // Print summary for multi-module projects or if requested
        if (results.size() > 1 || options.isSummaryOnly()) {
            printSummary(results, totalErrors, totalWarnings, totalInfos);
        }
    }
    
    private void printVersion() {
        System.out.println(BOLD + BLUE + TOOL_NAME + RESET);
        System.out.println("Version: " + VERSION);
        System.out.println("Part of the Firefly Platform Toolset");
    }
    
    private void printEnhancedHelp() {
        System.out.println();
        System.out.println(BOLD + BLUE + "🔥 " + TOOL_NAME + " - Version " + VERSION + RESET);
        System.out.println(CYAN + "Part of the Firefly Platform Toolset" + RESET);
        System.out.println();
        System.out.println(BOLD + "USAGE:" + RESET);
        System.out.println("  pom-validator [OPTIONS] <path>" );
        System.out.println();
        System.out.println(BOLD + "ARGUMENTS:" + RESET);
        System.out.println("  <path>              Path to a pom.xml file or directory containing POM files");
        System.out.println("                      If a directory, validates the pom.xml in that directory");
        System.out.println("                      Defaults to current directory (.) if not specified");
        System.out.println();
        System.out.println(BOLD + "OPTIONS:" + RESET);
        System.out.println("  " + GREEN + "Basic Options:" + RESET);
        System.out.println("    -h, --help          Show this help message");
        System.out.println("    -v, --version       Show version information");
        System.out.println("    -r, --recursive     Validate all POM files recursively");
        System.out.println("    -s, --summary       Show summary only (no detailed messages)");
        System.out.println();
        System.out.println("  " + BLUE + "Advanced Modes:" + RESET);
        System.out.println("    -w, --watch         Watch mode - monitor POMs for changes");
        System.out.println("    -i, --interactive   Interactive mode - fix issues interactively");
        System.out.println("    -f, --auto-fix      Automatically fix common issues");
        System.out.println();
        System.out.println("  " + YELLOW + "Output Options:" + RESET);
        System.out.println("    -o, --output <fmt>  Output format: console|json|xml|markdown|html|junit");
        System.out.println("    -O, --output-file   Write output to file");
        System.out.println("    -q, --quiet         Suppress all output");
        System.out.println("    -V, --verbose       Show detailed output");
        System.out.println("    --no-color          Disable colored output");
        System.out.println();
        System.out.println("  " + PURPLE + "Filtering:" + RESET);
        System.out.println("    -S, --severity <l>  Minimum severity: error|warning|info|all");
        System.out.println("    -p, --profile <p>   Validation profile: strict|standard|minimal");
        System.out.println("    -e, --exclude <p>   Exclude files matching pattern");
        System.out.println("    -I, --include <p>   Include only files matching pattern");
        System.out.println("    --fail-fast         Stop on first error");
        System.out.println();
        System.out.println("  " + CYAN + "Configuration:" + RESET);
        System.out.println("    -c, --config <f>    Use configuration file");
        System.out.println("    -D<prop>=<value>    Set a property value");
        System.out.println();
        System.out.println(BOLD + "EXAMPLES:" + RESET);
        System.out.println("  " + GREEN + "# Validate current directory's pom.xml" + RESET);
        System.out.println("  pom-validator");
        System.out.println("  pom-validator .");
        System.out.println();
        System.out.println("  " + GREEN + "# Validate a specific POM file" + RESET);
        System.out.println("  pom-validator /path/to/pom.xml");
        System.out.println();
        System.out.println("  " + GREEN + "# Validate a multi-module project (all modules)" + RESET);
        System.out.println("  pom-validator --recursive /path/to/multi-module-project");
        System.out.println("  pom-validator -r .");
        System.out.println();
        System.out.println("  " + GREEN + "# Show only summary for multi-module project" + RESET);
        System.out.println("  pom-validator -r -s /path/to/project");
        System.out.println();
        System.out.println("  " + GREEN + "# Watch mode for continuous validation" + RESET);
        System.out.println("  pom-validator --watch .");
        System.out.println();
        System.out.println("  " + GREEN + "# Interactive mode to fix issues" + RESET);
        System.out.println("  pom-validator --interactive pom.xml");
        System.out.println();
        System.out.println("  " + GREEN + "# Generate JSON report" + RESET);
        System.out.println("  pom-validator -r -o json -O report.json .");
        System.out.println();
        System.out.println("  " + GREEN + "# Filter by severity" + RESET);
        System.out.println("  pom-validator -S error --fail-fast .");
        System.out.println();
        System.out.println(BOLD + "EXIT CODES:" + RESET);
        System.out.println("  0   All validated POMs are valid (no errors)");
        System.out.println("  1   One or more POMs have validation errors");
        System.out.println();
        System.out.println(BOLD + "OUTPUT LEVELS:" + RESET);
        System.out.println("  " + RED + "❌ ERRORS" + RESET + "    Critical issues that make the POM invalid");
        System.out.println("  " + YELLOW + "⚠️  WARNINGS" + RESET + "  Issues that should be addressed");
        System.out.println("  " + CYAN + "ℹ️  INFO" + RESET + "      Informational messages and tips");
        System.out.println();
        System.out.println(BOLD + "MULTI-MODULE PROJECTS:" + RESET);
        System.out.println("  When validating multi-module projects with --recursive:");
        System.out.println("  • Validates the parent POM first");
        System.out.println("  • Then validates each module's POM");
        System.out.println("  • Detects module references and parent-child relationships");
        System.out.println("  • Reports aggregated statistics at the end");
        System.out.println();
        System.out.println(BOLD + "MORE INFORMATION:" + RESET);
        System.out.println("  GitHub: https://github.com/firefly-oss/pom-validator-tool");
        System.out.println("  Issues: https://github.com/firefly-oss/pom-validator-tool/issues");
        System.out.println();
    }
    
    
    private List<Path> findAllPoms(Path rootPath, CliOptions options) throws IOException {
        List<Path> poms = new ArrayList<>();
        
        Files.walkFileTree(rootPath, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                if (file.getFileName().toString().equals("pom.xml")) {
                    poms.add(file);
                }
                return FileVisitResult.CONTINUE;
            }
            
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                // Skip hidden directories and common non-source directories
                String dirName = dir.getFileName() != null ? dir.getFileName().toString() : "";
                if (dirName.startsWith(".") || dirName.equals("target") || dirName.equals("node_modules")) {
                    return FileVisitResult.SKIP_SUBTREE;
                }
                
                // Check exclude patterns
                String dirPath = dir.toString();
                for (String pattern : options.getExcludePatterns()) {
                    if (dirPath.contains(pattern)) {
                        return FileVisitResult.SKIP_SUBTREE;
                    }
                }
                
                return FileVisitResult.CONTINUE;
            }
        });
        
        return poms;
    }
    
    private List<Path> sortPomsParentFirst(List<Path> poms) {
        // Sort by depth (parent POMs are typically at higher levels)
        return poms.stream()
            .sorted(Comparator.comparingInt(p -> p.getNameCount()))
            .collect(Collectors.toList());
    }
    
    private void printValidationResult(Path pomPath, ValidationResult result, Path basePath) {
        String relativePath = basePath.relativize(pomPath).toString();
        if (relativePath.isEmpty()) {
            relativePath = pomPath.getFileName().toString();
        }
        
        System.out.println(BOLD + "=== " + relativePath + " ===" + RESET);
        System.out.println("Status: " + (result.isValid() ? GREEN + "✅ VALID" : RED + "❌ INVALID") + RESET);
        
        if (!result.getErrors().isEmpty()) {
            System.out.println();
            System.out.println(RED + "ERRORS:" + RESET);
            result.getErrors().forEach(error -> {
                System.out.println("  " + RED + "❌ " + error.getMessage() + RESET);
                if (error.hasSuggestion()) {
                    System.out.println("     💡 Fix: " + error.getSuggestion());
                }
            });
        }
        
        if (!result.getWarnings().isEmpty()) {
            System.out.println();
            System.out.println(YELLOW + "WARNINGS:" + RESET);
            result.getWarnings().forEach(warning -> {
                System.out.println("  " + YELLOW + "⚠️  " + warning.getMessage() + RESET);
                if (warning.hasSuggestion()) {
                    System.out.println("     💡 Suggestion: " + warning.getSuggestion());
                }
            });
        }
        
        if (!result.getInfos().isEmpty()) {
            System.out.println();
            System.out.println(CYAN + "INFO:" + RESET);
            result.getInfos().forEach(info -> {
                System.out.println("  " + CYAN + "ℹ️  " + info.getMessage() + RESET);
                if (info.hasSuggestion()) {
                    System.out.println("     💡 Tip: " + info.getSuggestion());
                }
            });
        }
        
        System.out.println();
        System.out.println("Summary: " + 
            (result.getErrors().size() > 0 ? RED : "") + result.getErrors().size() + " errors" + RESET + ", " +
            (result.getWarnings().size() > 0 ? YELLOW : "") + result.getWarnings().size() + " warnings" + RESET + ", " +
            result.getInfos().size() + " info messages");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println();
    }
    
    private void printSummary(Map<Path, ValidationResult> results, int totalErrors, int totalWarnings, int totalInfos) {
        System.out.println(BOLD + BLUE + "📊 OVERALL SUMMARY" + RESET);
        System.out.println("═══════════════════════════════════════════════════════════════");
        
        System.out.println(BOLD + "Total POMs validated: " + results.size() + RESET);
        
        // Count valid vs invalid
        long validCount = results.values().stream().filter(ValidationResult::isValid).count();
        long invalidCount = results.size() - validCount;
        
        System.out.println(GREEN + "  ✅ Valid: " + validCount + RESET);
        if (invalidCount > 0) {
            System.out.println(RED + "  ❌ Invalid: " + invalidCount + RESET);
        }
        
        System.out.println();
        System.out.println(BOLD + "Total issues found:" + RESET);
        System.out.println("  " + (totalErrors > 0 ? RED : "") + "Errors:   " + totalErrors + RESET);
        System.out.println("  " + (totalWarnings > 0 ? YELLOW : "") + "Warnings: " + totalWarnings + RESET);
        System.out.println("  " + CYAN + "Info:     " + totalInfos + RESET);
        
        // List POMs with errors
        if (invalidCount > 0) {
            System.out.println();
            System.out.println(RED + "POMs with errors:" + RESET);
            results.entrySet().stream()
                .filter(e -> !e.getValue().isValid())
                .forEach(e -> {
                    Path path = e.getKey();
                    ValidationResult result = e.getValue();
                    System.out.println("  • " + path.getFileName() + " (" + 
                        result.getErrors().size() + " errors, " + 
                        result.getWarnings().size() + " warnings)");
                });
        }
        
        System.out.println();
        System.out.println("═══════════════════════════════════════════════════════════════");
        System.out.println(totalErrors > 0 ? 
            RED + BOLD + "❌ VALIDATION FAILED" + RESET + " - Fix errors before proceeding" :
            GREEN + BOLD + "✅ VALIDATION PASSED" + RESET + " - All POMs are valid");
        System.out.println();
    }
}
