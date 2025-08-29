package com.firefly.tools.pomvalidator.cli.ui;

import com.firefly.tools.pomvalidator.model.ValidationIssue;
import com.firefly.tools.pomvalidator.model.ValidationResult;
import com.firefly.tools.pomvalidator.cli.CliOptions;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Enhanced console formatter with improved UX design.
 * Provides beautiful, informative, and user-friendly output.
 */
public class EnhancedConsoleFormatter {
    
    private final CliUI ui;
    private final CliOptions options;
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
    
    // Statistics
    private int totalFiles = 0;
    private int validFiles = 0;
    private int invalidFiles = 0;
    private int totalErrors = 0;
    private int totalWarnings = 0;
    private int totalInfos = 0;
    private long startTime;
    
    public EnhancedConsoleFormatter(CliOptions options) {
        this.options = options;
        this.ui = new CliUI(System.out, !options.isNoColor(), 80);
        this.startTime = System.currentTimeMillis();
    }
    
    /**
     * Print welcome banner
     */
    public void printWelcome() {
        ui.printHeader("POM VALIDATOR TOOL", '═');
        
        String[] banner = {
            ui.cyan("╔═══════════════════════════════════════════════════════╗"),
            ui.cyan("║") + "  " + ui.bold(ui.blue("📦 POM Validator Tool")) + " " + ui.dim("v1.0.0") + "              " + ui.cyan("║"),
            ui.cyan("║") + "  " + ui.gray("Enterprise-grade Maven POM validation") + "          " + ui.cyan("║"),
            ui.cyan("║") + "  " + ui.gray("Part of Firefly OpenCore Banking Platform") + "     " + ui.cyan("║"),
            ui.cyan("╚═══════════════════════════════════════════════════════╝")
        };
        
        for (String line : banner) {
            ui.println(line);
        }
        
        ui.newLine();
        ui.println(ui.dim("Started at " + LocalDateTime.now().format(timeFormatter)));
        ui.printDivider();
    }
    
    /**
     * Print scanning progress
     */
    public void printScanningStart(Path path) {
        ui.newLine();
        ui.println(ui.info("Scanning for POM files..."));
        ui.println(ui.dim("  Path: " + path.toAbsolutePath()));
        ui.println(ui.dim("  Mode: " + (options.isRecursive() ? "Recursive" : "Single file/directory")));
        
        if (!options.getExcludePatterns().isEmpty()) {
            ui.println(ui.dim("  Excluding: " + String.join(", ", options.getExcludePatterns())));
        }
        if (!options.getIncludePatterns().isEmpty()) {
            ui.println(ui.dim("  Including: " + String.join(", ", options.getIncludePatterns())));
        }
        
        ui.newLine();
    }
    
    /**
     * Print file processing
     */
    public void printProcessingFile(Path file, int current, int total) {
        totalFiles++;
        
        if (options.isQuiet()) return;
        
        if (options.isVerbose()) {
            ui.clearLine();
            ui.printProgress("Processing", current, total);
            ui.println("");
            ui.println(ui.cyan("  " + CliUI.FILE + " ") + file.getFileName());
        } else {
            ui.clearLine();
            ui.print(ui.cyan(CliUI.SEARCH + " ") + "Validating: " + ui.dim(file.getFileName().toString()));
        }
    }
    
    /**
     * Format single validation result with enhanced UI
     */
    public void formatResult(Path file, ValidationResult result) {
        if (options.isQuiet() && result.isValid()) {
            return;
        }
        
        ui.clearLine();
        
        // File header
        ui.newLine();
        printFileHeader(file, result);
        
        // Show issues by category
        if (!result.isValid() || !result.getWarnings().isEmpty() || !result.getInfos().isEmpty()) {
            if (!result.getErrors().isEmpty()) {
                printIssueCategory("ERRORS", result.getErrors(), ui.red("●"));
                totalErrors += result.getErrors().size();
            }
            
            if (!result.getWarnings().isEmpty() && shouldShowWarnings()) {
                printIssueCategory("WARNINGS", result.getWarnings(), ui.yellow("●"));
                totalWarnings += result.getWarnings().size();
            }
            
            if (!result.getInfos().isEmpty() && shouldShowInfo()) {
                printIssueCategory("INFO", result.getInfos(), ui.blue("●"));
                totalInfos += result.getInfos().size();
            }
            
            // Quick stats
            printQuickStats(result);
        }
        
        // Update counters
        if (result.isValid()) {
            validFiles++;
        } else {
            invalidFiles++;
        }
    }
    
    private void printFileHeader(Path file, ValidationResult result) {
        String status = result.isValid() ? 
            ui.green(CliUI.CHECK + " VALID") : 
            ui.red(CliUI.CROSS + " INVALID");
        
        String relativePath = getRelativePath(file);
        
        // Create a nice box header for the file
        String header = String.format("%s %s %s", 
            ui.bold(ui.cyan(CliUI.FILE)), 
            ui.bold(relativePath), 
            status);
        
        ui.println(header);
        ui.println(ui.gray("─".repeat(Math.min(80, relativePath.length() + 20))));
    }
    
    private void printIssueCategory(String category, List<ValidationIssue> issues, String bullet) {
        ui.newLine();
        ui.println(ui.bold("  " + category + " (" + issues.size() + ")"));
        
        for (ValidationIssue issue : issues) {
            // Issue message
            ui.println("    " + bullet + " " + issue.getMessage());
            
            // Suggestion with arrow
            if (issue.hasSuggestion()) {
                ui.println("      " + ui.cyan(CliUI.ARROW_RIGHT) + " " + 
                          ui.italic(ui.gray(issue.getSuggestion())));
            }
        }
    }
    
    private void printQuickStats(ValidationResult result) {
        ui.newLine();
        
        List<String> stats = new ArrayList<>();
        if (!result.getErrors().isEmpty()) {
            stats.add(ui.red(result.getErrors().size() + " errors"));
        }
        if (!result.getWarnings().isEmpty()) {
            stats.add(ui.yellow(result.getWarnings().size() + " warnings"));
        }
        if (!result.getInfos().isEmpty()) {
            stats.add(ui.blue(result.getInfos().size() + " info"));
        }
        
        if (!stats.isEmpty()) {
            ui.println("  " + ui.dim("Summary: " + String.join(", ", stats)));
        }
    }
    
    /**
     * Print final summary with enhanced design
     */
    public void printSummary() {
        long duration = System.currentTimeMillis() - startTime;
        
        ui.newLine();
        ui.printDivider('═');
        ui.printSection(ui.bold(ui.cyan("VALIDATION SUMMARY")));
        ui.newLine();
        
        // Create summary table
        List<List<String>> rows = new ArrayList<>();
        rows.add(Arrays.asList("Files Scanned", String.valueOf(totalFiles)));
        rows.add(Arrays.asList("Valid Files", ui.green(String.valueOf(validFiles))));
        rows.add(Arrays.asList("Invalid Files", ui.red(String.valueOf(invalidFiles))));
        rows.add(Arrays.asList("Total Errors", totalErrors > 0 ? ui.red(String.valueOf(totalErrors)) : "0"));
        rows.add(Arrays.asList("Total Warnings", totalWarnings > 0 ? ui.yellow(String.valueOf(totalWarnings)) : "0"));
        rows.add(Arrays.asList("Total Info", String.valueOf(totalInfos)));
        rows.add(Arrays.asList("Duration", formatDuration(duration)));
        
        List<String> headers = Arrays.asList("Metric", "Value");
        ui.printTable(rows, headers);
        
        ui.newLine();
        
        // Overall status
        printOverallStatus();
        
        // Tips
        if (invalidFiles > 0 && !options.isQuiet()) {
            printTips();
        }
        
        ui.printDivider('═');
    }
    
    private void printOverallStatus() {
        if (totalFiles == 0) {
            ui.println(ui.warning("No POM files found to validate"));
        } else if (invalidFiles == 0 && totalWarnings == 0) {
            ui.printBox(ui.success("All POM files are valid! " + CliUI.SPARKLES));
        } else if (invalidFiles == 0) {
            ui.printBox(ui.warning("Validation passed with warnings"));
        } else {
            ui.printBox(ui.error("Validation failed - " + invalidFiles + " invalid file(s)"));
        }
    }
    
    private void printTips() {
        ui.newLine();
        ui.printSection(ui.bold("💡 Quick Tips"));
        
        List<String> tips = new ArrayList<>();
        
        if (totalErrors > 0) {
            tips.add("Use " + ui.cyan("--interactive") + " to fix issues with guidance");
            tips.add("Use " + ui.cyan("--auto-fix") + " to automatically fix common issues");
        }
        
        if (!options.isVerbose()) {
            tips.add("Use " + ui.cyan("--verbose") + " for detailed output");
        }
        
        if (options.getSeverityLevel() != CliOptions.SeverityLevel.ALL) {
            tips.add("Use " + ui.cyan("-S all") + " to see all severity levels");
        }
        
        tips.add("Use " + ui.cyan("--help") + " to see all available options");
        
        ui.printList(tips);
    }
    
    /**
     * Print multi-module project tree
     */
    public void printProjectStructure(Map<Path, List<Path>> moduleStructure) {
        ui.newLine();
        ui.printSection(ui.bold(ui.cyan("PROJECT STRUCTURE")));
        ui.newLine();
        
        CliUI.TreeNode root = new CliUI.TreeNode(ui.bold("Root Project"));
        buildModuleTree(root, moduleStructure, null);
        ui.printTree(root);
    }
    
    private void buildModuleTree(CliUI.TreeNode parent, Map<Path, List<Path>> structure, Path currentPath) {
        List<Path> children = structure.get(currentPath);
        if (children != null) {
            for (Path child : children) {
                String name = child.getFileName().toString();
                CliUI.TreeNode childNode = parent.addChild(name);
                buildModuleTree(childNode, structure, child);
            }
        }
    }
    
    /**
     * Format validation in watch mode
     */
    public void printWatchModeHeader() {
        ui.printHeader("WATCH MODE ACTIVE", '═');
        ui.println(ui.info("Monitoring for POM file changes..."));
        ui.println(ui.dim("Press Ctrl+C to stop"));
        ui.printDivider();
    }
    
    public void printFileChanged(Path file) {
        ui.clearLine();
        ui.println(ui.warning("File changed: " + file.getFileName()));
    }
    
    /**
     * Interactive mode formatting
     */
    public void printInteractiveMenu(List<String> options) {
        ui.newLine();
        ui.printSection(ui.bold("Select an action:"));
        ui.printNumberedList(options);
        ui.newLine();
        ui.print(ui.cyan("Enter choice: "));
    }
    
    // Utility methods
    
    private boolean shouldShowWarnings() {
        return options.getSeverityLevel().includes(CliOptions.SeverityLevel.WARNING);
    }
    
    private boolean shouldShowInfo() {
        return options.getSeverityLevel().includes(CliOptions.SeverityLevel.INFO);
    }
    
    private String getRelativePath(Path file) {
        try {
            Path currentDir = Path.of(System.getProperty("user.dir"));
            return currentDir.relativize(file).toString();
        } catch (Exception e) {
            return file.toString();
        }
    }
    
    private String formatDuration(long millis) {
        if (millis < 1000) {
            return millis + "ms";
        } else if (millis < 60000) {
            return String.format("%.1fs", millis / 1000.0);
        } else {
            long minutes = millis / 60000;
            long seconds = (millis % 60000) / 1000;
            return String.format("%dm %ds", minutes, seconds);
        }
    }
    
    /**
     * Print error with stack trace
     */
    public void printError(String message, Throwable error) {
        ui.newLine();
        ui.println(ui.error(message));
        
        if (options.isVerbose() && error != null) {
            ui.println(ui.dim("Stack trace:"));
            for (StackTraceElement element : error.getStackTrace()) {
                ui.println(ui.gray("  at " + element.toString()));
                if (!options.isVerbose()) {
                    // Show only first 5 lines in non-verbose mode
                    break;
                }
            }
        } else if (error != null) {
            ui.println(ui.dim("Use --verbose to see full stack trace"));
        }
    }
}
