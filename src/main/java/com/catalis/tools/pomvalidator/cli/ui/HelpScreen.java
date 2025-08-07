package com.catalis.tools.pomvalidator.cli.ui;

import java.util.*;

/**
 * Enhanced help screen with better organization and visual design.
 */
public class HelpScreen {
    
    private final CliUI ui;
    
    public HelpScreen() {
        this.ui = new CliUI();
    }
    
    public void show() {
        showHeader();
        showUsage();
        showCommonCommands();
        showOptions();
        showExamples();
        showFooter();
    }
    
    private void showHeader() {
        ui.printHeader("POM VALIDATOR TOOL - HELP", '═');
        
        ui.println(ui.bold(ui.cyan("📦 POM Validator Tool")) + " " + ui.dim("v1.0.0"));
        ui.println(ui.gray("Enterprise-grade Maven POM validation tool"));
        ui.println(ui.gray("Part of Firefly OpenCore Banking Platform"));
        ui.newLine();
    }
    
    private void showUsage() {
        ui.printSection(ui.bold("USAGE"));
        ui.println("  pom-validator [OPTIONS] [PATH]");
        ui.newLine();
        ui.println("  PATH: File or directory to validate (default: current directory)");
        ui.newLine();
    }
    
    private void showCommonCommands() {
        ui.printSection(ui.bold("🚀 COMMON COMMANDS"));
        
        List<List<String>> commands = Arrays.asList(
            Arrays.asList(ui.cyan("pom-validator"), "Validate current directory"),
            Arrays.asList(ui.cyan("pom-validator pom.xml"), "Validate specific file"),
            Arrays.asList(ui.cyan("pom-validator -r ."), "Validate recursively"),
            Arrays.asList(ui.cyan("pom-validator -i pom.xml"), "Interactive fix mode"),
            Arrays.asList(ui.cyan("pom-validator -f pom.xml"), "Auto-fix common issues"),
            Arrays.asList(ui.cyan("pom-validator -w ."), "Watch mode for live validation")
        );
        
        for (List<String> cmd : commands) {
            ui.println(String.format("  %-35s %s", cmd.get(0), ui.dim(cmd.get(1))));
        }
        ui.newLine();
    }
    
    private void showOptions() {
        ui.printSection(ui.bold("OPTIONS"));
        
        // Group options by category
        showBasicOptions();
        showModeOptions();
        showFilterOptions();
        showOutputOptions();
        showAdvancedOptions();
    }
    
    private void showBasicOptions() {
        ui.printSubHeader("Basic Options");
        
        List<Option> options = Arrays.asList(
            new Option("-h, --help", "Show this help message"),
            new Option("-v, --version", "Show version information"),
            new Option("-r, --recursive", "Validate all POMs in directory tree"),
            new Option("-s, --summary", "Show summary only (no individual file details)")
        );
        
        printOptions(options);
    }
    
    private void showModeOptions() {
        ui.printSubHeader("Validation Modes");
        
        List<Option> options = Arrays.asList(
            new Option("-i, --interactive", "Interactive mode with guided fixes"),
            new Option("-f, --auto-fix", "Automatically fix common issues"),
            new Option("-w, --watch", "Watch files for changes and re-validate"),
            new Option("-p, --profile " + ui.dim("<profile>"), "Validation profile: strict|standard|minimal")
        );
        
        printOptions(options);
    }
    
    private void showFilterOptions() {
        ui.printSubHeader("Filtering");
        
        List<Option> options = Arrays.asList(
            new Option("-e, --exclude " + ui.dim("<pattern>"), "Exclude paths matching pattern"),
            new Option("-I, --include " + ui.dim("<pattern>"), "Include only paths matching pattern"),
            new Option("-S, --severity " + ui.dim("<level>"), "Minimum severity: error|warning|info|all")
        );
        
        printOptions(options);
    }
    
    private void showOutputOptions() {
        ui.printSubHeader("Output Control");
        
        List<Option> options = Arrays.asList(
            new Option("-o, --output " + ui.dim("<format>"), "Output format: console|json|markdown|xml|html|junit"),
            new Option("-O, --output-file " + ui.dim("<file>"), "Write output to file"),
            new Option("-q, --quiet", "Suppress output except errors"),
            new Option("-V, --verbose", "Show detailed output"),
            new Option("--no-color", "Disable colored output"),
            new Option("--fail-fast", "Stop on first error")
        );
        
        printOptions(options);
    }
    
    private void showAdvancedOptions() {
        ui.printSubHeader("Advanced");
        
        List<Option> options = Arrays.asList(
            new Option("-c, --config " + ui.dim("<file>"), "Use custom configuration file"),
            new Option("-D" + ui.dim("<property=value>"), "Set system property")
        );
        
        printOptions(options);
    }
    
    private void printOptions(List<Option> options) {
        for (Option opt : options) {
            ui.println(String.format("  %-30s %s", opt.flag, ui.gray(opt.description)));
        }
        ui.newLine();
    }
    
    private void showExamples() {
        ui.printSection(ui.bold("📚 EXAMPLES"));
        
        List<Example> examples = Arrays.asList(
            new Example(
                "Validate current project",
                "pom-validator"
            ),
            new Example(
                "Validate with auto-fix",
                "pom-validator --auto-fix pom.xml"
            ),
            new Example(
                "Recursive validation excluding target",
                "pom-validator -r -e target ."
            ),
            new Example(
                "Generate JSON report",
                "pom-validator -o json -O report.json ."
            ),
            new Example(
                "Watch mode with verbose output",
                "pom-validator -w -V ."
            ),
            new Example(
                "Interactive mode for multi-module project",
                "pom-validator -i -r ."
            ),
            new Example(
                "CI/CD pipeline validation",
                "pom-validator -r -S error --fail-fast ."
            )
        );
        
        for (Example ex : examples) {
            ui.println("  " + ui.dim("# " + ex.description));
            ui.println("  " + ui.cyan("$ " + ex.command));
            ui.newLine();
        }
    }
    
    private void showFooter() {
        ui.printDivider('─');
        
        ui.printSection(ui.bold("📖 MORE INFORMATION"));
        
        List<List<String>> links = Arrays.asList(
            Arrays.asList("Documentation", "https://github.com/firefly-oss/pom-validator-tool/wiki"),
            Arrays.asList("Report Issues", "https://github.com/firefly-oss/pom-validator-tool/issues"),
            Arrays.asList("Discussions", "https://github.com/firefly-oss/pom-validator-tool/discussions")
        );
        
        for (List<String> link : links) {
            ui.println(String.format("  %-15s %s", link.get(0) + ":", ui.blue(ui.underline(link.get(1)))));
        }
        
        ui.newLine();
        ui.printDivider('═');
        ui.println(ui.center(ui.dim("Firefly OpenCore Banking Platform © 2024")));
    }
    
    // Helper classes
    
    private static class Option {
        final String flag;
        final String description;
        
        Option(String flag, String description) {
            this.flag = flag;
            this.description = description;
        }
    }
    
    private static class Example {
        final String description;
        final String command;
        
        Example(String description, String command) {
            this.description = description;
            this.command = command;
        }
    }
}
