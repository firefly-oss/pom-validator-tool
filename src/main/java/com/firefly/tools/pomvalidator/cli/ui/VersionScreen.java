package com.firefly.tools.pomvalidator.cli.ui;

/**
 * Version screen with enhanced visual design.
 */
public class VersionScreen {
    
    private final CliUI ui;
    
    public VersionScreen() {
        this.ui = new CliUI();
    }
    
    public void show() {
        ui.printHeader("VERSION INFO", '═');
        
        ui.println(ui.bold(ui.cyan("📦 POM Validator Tool")) + " " + ui.green("v1.0.0"));
        ui.newLine();
        
        ui.printSection(ui.bold("BUILD INFORMATION"));
        ui.println("  Version:     " + ui.green("1.0.0-SNAPSHOT"));
        ui.println("  Build Date:  " + ui.gray("2024.01.release"));
        ui.println("  Java:        " + ui.gray(System.getProperty("java.version")));
        ui.println("  JVM:         " + ui.gray(System.getProperty("java.vm.name")));
        ui.println("  OS:          " + ui.gray(System.getProperty("os.name") + " " + System.getProperty("os.version")));
        ui.newLine();
        
        ui.printSection(ui.bold("FEATURES"));
        ui.println("  ✓ " + ui.gray("Multi-module project support"));
        ui.println("  ✓ " + ui.gray("Parent POM inheritance validation"));
        ui.println("  ✓ " + ui.gray("Interactive fix mode"));
        ui.println("  ✓ " + ui.gray("Auto-fix common issues"));
        ui.println("  ✓ " + ui.gray("Watch mode for continuous validation"));
        ui.println("  ✓ " + ui.gray("Multiple output formats (JSON, Markdown, XML)"));
        ui.println("  ✓ " + ui.gray("Configurable validation profiles"));
        ui.newLine();
        
        ui.printSection(ui.bold("LICENSE"));
        ui.println("  " + ui.dim("Apache License 2.0"));
        ui.println("  " + ui.dim("© 2024 Firefly OpenCore"));
        ui.newLine();
        
        ui.printSection(ui.bold("CONTRIBUTORS"));
        ui.println("  " + ui.dim("Firefly OpenCore Team"));
        ui.println("  " + ui.dim("Community Contributors"));
        ui.newLine();
        
        ui.printDivider('─');
        ui.println(ui.dim("Part of Firefly OpenCore Banking Platform"));
        ui.println(ui.dim("For more information: ") + ui.blue(ui.underline("https://github.com/firefly-oss")));
        ui.printDivider('═');
    }
}
