package com.firefly.tools.pomvalidator.cli;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Command-line options parser for the POM Validator Tool.
 * Supports various output formats, filtering, and advanced features.
 */
public class CliOptions {
    
    public enum OutputFormat {
        CONSOLE("console"),
        JSON("json"),
        XML("xml"),
        MARKDOWN("markdown"),
        HTML("html"),
        JUNIT("junit");
        
        private final String value;
        
        OutputFormat(String value) {
            this.value = value;
        }
        
        public static OutputFormat fromString(String value) {
            for (OutputFormat format : values()) {
                if (format.value.equalsIgnoreCase(value)) {
                    return format;
                }
            }
            return CONSOLE;
        }
    }
    
    public enum SeverityLevel {
        ERROR("error", 1),
        WARNING("warning", 2),
        INFO("info", 3),
        ALL("all", 4);
        
        private final String value;
        private final int level;
        
        SeverityLevel(String value, int level) {
            this.value = value;
            this.level = level;
        }
        
        public static SeverityLevel fromString(String value) {
            for (SeverityLevel level : values()) {
                if (level.value.equalsIgnoreCase(value)) {
                    return level;
                }
            }
            return ALL;
        }
        
        public boolean includes(SeverityLevel other) {
            return this.level >= other.level;
        }
    }
    
    public enum ValidationProfile {
        STRICT("strict"),      // All validations enabled
        STANDARD("standard"),  // Default validations
        MINIMAL("minimal"),    // Only critical validations
        CUSTOM("custom");      // User-defined profile
        
        private final String value;
        
        ValidationProfile(String value) {
            this.value = value;
        }
        
        public static ValidationProfile fromString(String value) {
            for (ValidationProfile profile : values()) {
                if (profile.value.equalsIgnoreCase(value)) {
                    return profile;
                }
            }
            return STANDARD;
        }
    }
    
    private Path targetPath = Paths.get(".");
    private boolean recursive = false;
    private boolean summaryOnly = false;
    private boolean help = false;
    private boolean version = false;
    private boolean watch = false;
    private boolean interactive = false;
    private boolean autoFix = false;
    private boolean quiet = false;
    private boolean verbose = false;
    private boolean noColor = false;
    private boolean failFast = false;
    private OutputFormat outputFormat = OutputFormat.CONSOLE;
    private SeverityLevel severityLevel = SeverityLevel.ALL;
    private ValidationProfile profile = ValidationProfile.STANDARD;
    private Path outputFile = null;
    private Path configFile = null;
    private List<String> excludePatterns = new ArrayList<>();
    private List<String> includePatterns = new ArrayList<>();
    private Map<String, String> properties = new HashMap<>();
    
    public static CliOptions parse(String[] args) {
        CliOptions options = new CliOptions();
        
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            
            // Help and version
            if (isHelpFlag(arg)) {
                options.help = true;
                return options;
            }
            if (isVersionFlag(arg)) {
                options.version = true;
                return options;
            }
            
            // Boolean flags
            if ("--recursive".equals(arg) || "-r".equals(arg)) {
                options.recursive = true;
            } else if ("--summary".equals(arg) || "-s".equals(arg)) {
                options.summaryOnly = true;
            } else if ("--watch".equals(arg) || "-w".equals(arg)) {
                options.watch = true;
            } else if ("--interactive".equals(arg) || "-i".equals(arg)) {
                options.interactive = true;
            } else if ("--auto-fix".equals(arg) || "-f".equals(arg)) {
                options.autoFix = true;
            } else if ("--quiet".equals(arg) || "-q".equals(arg)) {
                options.quiet = true;
            } else if ("--verbose".equals(arg) || "-V".equals(arg)) {
                options.verbose = true;
            } else if ("--no-color".equals(arg)) {
                options.noColor = true;
            } else if ("--fail-fast".equals(arg)) {
                options.failFast = true;
            }
            // Options with values
            else if ("--output".equals(arg) || "-o".equals(arg)) {
                if (i + 1 < args.length) {
                    options.outputFormat = OutputFormat.fromString(args[++i]);
                }
            } else if ("--output-file".equals(arg) || "-O".equals(arg)) {
                if (i + 1 < args.length) {
                    options.outputFile = Paths.get(args[++i]);
                }
            } else if ("--severity".equals(arg) || "-S".equals(arg)) {
                if (i + 1 < args.length) {
                    options.severityLevel = SeverityLevel.fromString(args[++i]);
                }
            } else if ("--profile".equals(arg) || "-p".equals(arg)) {
                if (i + 1 < args.length) {
                    options.profile = ValidationProfile.fromString(args[++i]);
                }
            } else if ("--config".equals(arg) || "-c".equals(arg)) {
                if (i + 1 < args.length) {
                    options.configFile = Paths.get(args[++i]);
                }
            } else if ("--exclude".equals(arg) || "-e".equals(arg)) {
                if (i + 1 < args.length) {
                    options.excludePatterns.add(args[++i]);
                }
            } else if ("--include".equals(arg) || "-I".equals(arg)) {
                if (i + 1 < args.length) {
                    options.includePatterns.add(args[++i]);
                }
            } else if (arg.startsWith("-D")) {
                // Property definition like -Dproperty=value
                String prop = arg.substring(2);
                int eqIndex = prop.indexOf('=');
                if (eqIndex > 0) {
                    String key = prop.substring(0, eqIndex);
                    String value = prop.substring(eqIndex + 1);
                    options.properties.put(key, value);
                }
            }
            // Path argument
            else if (!arg.startsWith("-")) {
                options.targetPath = Paths.get(arg).toAbsolutePath();
            }
        }
        
        return options;
    }
    
    private static boolean isHelpFlag(String arg) {
        return "--help".equals(arg) || "-h".equals(arg) || "help".equals(arg) || "?".equals(arg);
    }
    
    private static boolean isVersionFlag(String arg) {
        return "--version".equals(arg) || "-v".equals(arg) || "version".equals(arg);
    }
    
    // Getters
    public Path getTargetPath() { return targetPath; }
    public boolean isRecursive() { return recursive; }
    public boolean isSummaryOnly() { return summaryOnly; }
    public boolean isHelp() { return help; }
    public boolean isVersion() { return version; }
    public boolean isWatch() { return watch; }
    public boolean isInteractive() { return interactive; }
    public boolean isAutoFix() { return autoFix; }
    public boolean isQuiet() { return quiet; }
    public boolean isVerbose() { return verbose; }
    public boolean isNoColor() { return noColor; }
    public boolean isFailFast() { return failFast; }
    public OutputFormat getOutputFormat() { return outputFormat; }
    public SeverityLevel getSeverityLevel() { return severityLevel; }
    public ValidationProfile getProfile() { return profile; }
    public Path getOutputFile() { return outputFile; }
    public Path getConfigFile() { return configFile; }
    public List<String> getExcludePatterns() { return excludePatterns; }
    public List<String> getIncludePatterns() { return includePatterns; }
    public Map<String, String> getProperties() { return properties; }
}
