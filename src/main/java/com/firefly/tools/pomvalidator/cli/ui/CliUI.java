package com.firefly.tools.pomvalidator.cli.ui;

import java.io.PrintStream;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Enhanced CLI UI components for better user experience.
 * Provides colored output, progress indicators, tables, and formatted displays.
 */
public class CliUI {
    
    // ANSI color codes
    public static final String RESET = "\u001B[0m";
    public static final String BLACK = "\u001B[30m";
    public static final String RED = "\u001B[31m";
    public static final String GREEN = "\u001B[32m";
    public static final String YELLOW = "\u001B[33m";
    public static final String BLUE = "\u001B[34m";
    public static final String PURPLE = "\u001B[35m";
    public static final String CYAN = "\u001B[36m";
    public static final String WHITE = "\u001B[37m";
    public static final String GRAY = "\u001B[90m";
    
    // ANSI style codes
    public static final String BOLD = "\u001B[1m";
    public static final String DIM = "\u001B[2m";
    public static final String ITALIC = "\u001B[3m";
    public static final String UNDERLINE = "\u001B[4m";
    
    // Unicode symbols
    public static final String CHECK = "✓";
    public static final String CROSS = "✗";
    public static final String WARNING = "⚠";
    public static final String INFO = "ℹ";
    public static final String ARROW_RIGHT = "→";
    public static final String BULLET = "•";
    public static final String STAR = "★";
    public static final String CIRCLE = "○";
    public static final String FILLED_CIRCLE = "●";
    public static final String SEARCH = "🔍";
    public static final String WRENCH = "🔧";
    public static final String ROCKET = "🚀";
    public static final String PACKAGE = "📦";
    public static final String FOLDER = "📁";
    public static final String FILE = "📄";
    public static final String CLOCK = "⏱";
    public static final String SPARKLES = "✨";
    
    private final PrintStream out;
    private final boolean colorEnabled;
    private final int terminalWidth;
    
    // Static color control for global disabling
    private static boolean globalColorEnabled = true;
    
    public CliUI() {
        this(System.out, true, getTerminalWidth());
    }
    
    public CliUI(PrintStream out, boolean colorEnabled, int terminalWidth) {
        this.out = out;
        this.colorEnabled = colorEnabled && globalColorEnabled;
        this.terminalWidth = terminalWidth;
    }
    
    /**
     * Globally enable or disable colored output.
     * @param enabled true to enable colors, false to disable
     */
    public static void setColorEnabled(boolean enabled) {
        globalColorEnabled = enabled;
    }
    
    // Color methods
    public String color(String text, String color) {
        return colorEnabled ? color + text + RESET : text;
    }
    
    public String red(String text) { return color(text, RED); }
    public String green(String text) { return color(text, GREEN); }
    public String yellow(String text) { return color(text, YELLOW); }
    public String blue(String text) { return color(text, BLUE); }
    public String purple(String text) { return color(text, PURPLE); }
    public String cyan(String text) { return color(text, CYAN); }
    public String gray(String text) { return color(text, GRAY); }
    public String bold(String text) { return color(text, BOLD); }
    public String dim(String text) { return color(text, DIM); }
    public String italic(String text) { return color(text, ITALIC); }
    public String underline(String text) { return color(text, UNDERLINE); }
    
    // Status indicators
    public String success(String message) {
        return green(CHECK + " ") + message;
    }
    
    public String error(String message) {
        return red(CROSS + " ") + message;
    }
    
    public String warning(String message) {
        return yellow(WARNING + " ") + message;
    }
    
    public String info(String message) {
        return blue(INFO + " ") + message;
    }
    
    // Headers and sections
    public void printHeader(String title) {
        printHeader(title, '=');
    }
    
    public void printHeader(String title, char borderChar) {
        String border = String.join("", Collections.nCopies(terminalWidth, String.valueOf(borderChar)));
        out.println();
        out.println(bold(blue(border)));
        out.println(bold(cyan(center(title))));
        out.println(bold(blue(border)));
        out.println();
    }
    
    public void printSubHeader(String title) {
        out.println();
        out.println(bold(cyan("▶ " + title)));
        out.println(gray(String.join("", Collections.nCopies(title.length() + 2, "─"))));
    }
    
    public void printSection(String title) {
        out.println();
        out.println(bold(title));
    }
    
    // Progress indicators
    public void printProgress(String task, int current, int total) {
        int percentage = (int) ((current / (double) total) * 100);
        int barWidth = Math.min(40, terminalWidth - 20);
        int filled = (int) ((current / (double) total) * barWidth);
        
        StringBuilder bar = new StringBuilder();
        bar.append("[");
        for (int i = 0; i < barWidth; i++) {
            if (i < filled) {
                bar.append(green("█"));
            } else {
                bar.append(gray("░"));
            }
        }
        bar.append("] ");
        bar.append(String.format("%3d%%", percentage));
        
        out.print("\r" + task + " " + bar);
        if (current >= total) {
            out.println();
        }
    }
    
    public void printSpinner(String message, int step) {
        String[] spinner = {"⠋", "⠙", "⠹", "⠸", "⠼", "⠴", "⠦", "⠧", "⠇", "⠏"};
        out.print("\r" + cyan(spinner[step % spinner.length]) + " " + message);
    }
    
    // Tables
    public void printTable(List<List<String>> rows, List<String> headers) {
        if (rows.isEmpty()) return;
        
        // Calculate column widths
        int[] widths = new int[headers.size()];
        for (int i = 0; i < headers.size(); i++) {
            widths[i] = headers.get(i).length();
        }
        
        for (List<String> row : rows) {
            for (int i = 0; i < Math.min(row.size(), widths.length); i++) {
                widths[i] = Math.max(widths[i], row.get(i).length());
            }
        }
        
        // Print header
        printTableBorder(widths, '┌', '┬', '┐');
        printTableRow(headers, widths, true);
        printTableBorder(widths, '├', '┼', '┤');
        
        // Print rows
        for (List<String> row : rows) {
            printTableRow(row, widths, false);
        }
        
        printTableBorder(widths, '└', '┴', '┘');
    }
    
    private void printTableBorder(int[] widths, char left, char middle, char right) {
        out.print(gray(String.valueOf(left)));
        for (int i = 0; i < widths.length; i++) {
            out.print(gray(String.join("", Collections.nCopies(widths[i] + 2, "─"))));
            if (i < widths.length - 1) {
                out.print(gray(String.valueOf(middle)));
            }
        }
        out.println(gray(String.valueOf(right)));
    }
    
    private void printTableRow(List<String> row, int[] widths, boolean isHeader) {
        out.print(gray("│"));
        for (int i = 0; i < widths.length; i++) {
            String cell = i < row.size() ? row.get(i) : "";
            String padded = String.format(" %-" + widths[i] + "s ", cell);
            out.print(isHeader ? bold(cyan(padded)) : padded);
            out.print(gray("│"));
        }
        out.println();
    }
    
    // Lists
    public void printList(List<String> items) {
        printList(items, BULLET);
    }
    
    public void printList(List<String> items, String bullet) {
        for (String item : items) {
            out.println("  " + cyan(bullet) + " " + item);
        }
    }
    
    public void printNumberedList(List<String> items) {
        for (int i = 0; i < items.size(); i++) {
            out.println(String.format("  %s. %s", cyan(String.valueOf(i + 1)), items.get(i)));
        }
    }
    
    // Box drawing
    public void printBox(String content) {
        printBox(content, "─", "│", "┌", "┐", "└", "┘");
    }
    
    public void printBox(String content, String h, String v, String tl, String tr, String bl, String br) {
        String[] lines = content.split("\n");
        int maxWidth = 0;
        for (String line : lines) {
            maxWidth = Math.max(maxWidth, removeAnsiCodes(line).length());
        }
        
        // Top border
        out.print(gray(tl));
        out.print(gray(String.join("", Collections.nCopies(maxWidth + 2, h))));
        out.println(gray(tr));
        
        // Content
        for (String line : lines) {
            out.print(gray(v + " "));
            out.print(line);
            int padding = maxWidth - removeAnsiCodes(line).length();
            out.print(String.join("", Collections.nCopies(padding, " ")));
            out.println(gray(" " + v));
        }
        
        // Bottom border
        out.print(gray(bl));
        out.print(gray(String.join("", Collections.nCopies(maxWidth + 2, h))));
        out.println(gray(br));
    }
    
    // Tree structure
    public void printTree(TreeNode root) {
        printTreeNode(root, "", true);
    }
    
    private void printTreeNode(TreeNode node, String prefix, boolean isLast) {
        out.print(gray(prefix));
        out.print(gray(isLast ? "└── " : "├── "));
        out.println(node.name);
        
        List<TreeNode> children = node.children;
        for (int i = 0; i < children.size(); i++) {
            String childPrefix = prefix + (isLast ? "    " : "│   ");
            printTreeNode(children.get(i), childPrefix, i == children.size() - 1);
        }
    }
    
    // Utility methods
    public String center(String text) {
        int padding = (terminalWidth - removeAnsiCodes(text).length()) / 2;
        return String.join("", Collections.nCopies(Math.max(0, padding), " ")) + text;
    }
    
    public String pad(String text, int width) {
        int textLength = removeAnsiCodes(text).length();
        if (textLength >= width) return text;
        return text + String.join("", Collections.nCopies(width - textLength, " "));
    }
    
    public void printDivider() {
        printDivider('─');
    }
    
    public void printDivider(char dividerChar) {
        out.println(gray(String.join("", Collections.nCopies(terminalWidth, String.valueOf(dividerChar)))));
    }
    
    public void newLine() {
        out.println();
    }
    
    public void print(String text) {
        out.print(text);
    }
    
    public void println(String text) {
        out.println(text);
    }
    
    public void clearLine() {
        out.print("\r" + String.join("", Collections.nCopies(terminalWidth, " ")) + "\r");
    }
    
    // Animated effects
    public void printWithDelay(String text, long delayMs) {
        for (char c : text.toCharArray()) {
            out.print(c);
            out.flush();
            try {
                Thread.sleep(delayMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        out.println();
    }
    
    public void printTypewriter(String text) {
        printWithDelay(text, 50);
    }
    
    // Helper methods
    private String removeAnsiCodes(String text) {
        return text.replaceAll("\u001B\\[[;\\d]*m", "");
    }
    
    private static int getTerminalWidth() {
        try {
            String os = System.getProperty("os.name").toLowerCase();
            Process process;
            if (os.contains("win")) {
                // Windows
                process = Runtime.getRuntime().exec(new String[]{"cmd", "/c", "mode", "con"});
            } else {
                // Unix/Linux/Mac
                process = Runtime.getRuntime().exec(new String[]{"sh", "-c", "tput cols"});
            }
            
            process.waitFor();
            try (java.util.Scanner scanner = new java.util.Scanner(process.getInputStream())) {
                if (os.contains("win")) {
                    // Parse Windows output
                    while (scanner.hasNextLine()) {
                        String line = scanner.nextLine();
                        if (line.trim().startsWith("Columns:")) {
                            return Integer.parseInt(line.split(":")[1].trim());
                        }
                    }
                } else {
                    // Parse Unix output
                    if (scanner.hasNextInt()) {
                        return scanner.nextInt();
                    }
                }
            }
        } catch (Exception e) {
            // Ignore and use default
        }
        return 80; // Default width
    }
    
    // Tree node class for tree printing
    public static class TreeNode {
        public String name;
        public List<TreeNode> children;
        
        public TreeNode(String name) {
            this.name = name;
            this.children = new java.util.ArrayList<>();
        }
        
        public TreeNode addChild(String name) {
            TreeNode child = new TreeNode(name);
            children.add(child);
            return child;
        }
    }
}
