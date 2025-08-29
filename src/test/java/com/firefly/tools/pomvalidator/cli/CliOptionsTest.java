package com.firefly.tools.pomvalidator.cli;

import org.junit.jupiter.api.Test;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

class CliOptionsTest {
    
    @Test
    void testDefaultOptions() {
        CliOptions options = CliOptions.parse(new String[]{});
        
        assertEquals(Paths.get("."), options.getTargetPath());
        assertFalse(options.isRecursive());
        assertFalse(options.isSummaryOnly());
        assertFalse(options.isHelp());
        assertFalse(options.isVersion());
        assertFalse(options.isWatch());
        assertFalse(options.isInteractive());
        assertFalse(options.isAutoFix());
        assertEquals(CliOptions.OutputFormat.CONSOLE, options.getOutputFormat());
        assertEquals(CliOptions.SeverityLevel.ALL, options.getSeverityLevel());
        assertEquals(CliOptions.ValidationProfile.STANDARD, options.getProfile());
    }
    
    @Test
    void testHelpFlags() {
        assertTrue(CliOptions.parse(new String[]{"--help"}).isHelp());
        assertTrue(CliOptions.parse(new String[]{"-h"}).isHelp());
        assertTrue(CliOptions.parse(new String[]{"help"}).isHelp());
        assertTrue(CliOptions.parse(new String[]{"?"}).isHelp());
    }
    
    @Test
    void testVersionFlags() {
        assertTrue(CliOptions.parse(new String[]{"--version"}).isVersion());
        assertTrue(CliOptions.parse(new String[]{"-v"}).isVersion());
        assertTrue(CliOptions.parse(new String[]{"version"}).isVersion());
    }
    
    @Test
    void testBooleanFlags() {
        String[] args = {"-r", "-s", "-w", "-i", "-f", "-q", "-V", "--no-color", "--fail-fast", "."};
        CliOptions options = CliOptions.parse(args);
        
        assertTrue(options.isRecursive());
        assertTrue(options.isSummaryOnly());
        assertTrue(options.isWatch());
        assertTrue(options.isInteractive());
        assertTrue(options.isAutoFix());
        assertTrue(options.isQuiet());
        assertTrue(options.isVerbose());
        assertTrue(options.isNoColor());
        assertTrue(options.isFailFast());
    }
    
    @Test
    void testOutputFormat() {
        assertEquals(CliOptions.OutputFormat.JSON, 
            CliOptions.parse(new String[]{"-o", "json"}).getOutputFormat());
        assertEquals(CliOptions.OutputFormat.MARKDOWN, 
            CliOptions.parse(new String[]{"--output", "markdown"}).getOutputFormat());
        assertEquals(CliOptions.OutputFormat.XML, 
            CliOptions.parse(new String[]{"-o", "xml"}).getOutputFormat());
        assertEquals(CliOptions.OutputFormat.HTML, 
            CliOptions.parse(new String[]{"-o", "html"}).getOutputFormat());
        assertEquals(CliOptions.OutputFormat.JUNIT, 
            CliOptions.parse(new String[]{"-o", "junit"}).getOutputFormat());
        
        // Test invalid format defaults to console
        assertEquals(CliOptions.OutputFormat.CONSOLE, 
            CliOptions.parse(new String[]{"-o", "invalid"}).getOutputFormat());
    }
    
    @Test
    void testOutputFile() {
        CliOptions options = CliOptions.parse(new String[]{"-O", "report.json"});
        assertEquals(Paths.get("report.json"), options.getOutputFile());
        
        options = CliOptions.parse(new String[]{"--output-file", "/tmp/report.md"});
        assertEquals(Paths.get("/tmp/report.md"), options.getOutputFile());
    }
    
    @Test
    void testSeverityLevel() {
        assertEquals(CliOptions.SeverityLevel.ERROR, 
            CliOptions.parse(new String[]{"-S", "error"}).getSeverityLevel());
        assertEquals(CliOptions.SeverityLevel.WARNING, 
            CliOptions.parse(new String[]{"--severity", "warning"}).getSeverityLevel());
        assertEquals(CliOptions.SeverityLevel.INFO, 
            CliOptions.parse(new String[]{"-S", "info"}).getSeverityLevel());
        assertEquals(CliOptions.SeverityLevel.ALL, 
            CliOptions.parse(new String[]{"-S", "all"}).getSeverityLevel());
        
        // Test invalid severity defaults to ALL
        assertEquals(CliOptions.SeverityLevel.ALL, 
            CliOptions.parse(new String[]{"-S", "invalid"}).getSeverityLevel());
    }
    
    @Test
    void testValidationProfile() {
        assertEquals(CliOptions.ValidationProfile.STRICT, 
            CliOptions.parse(new String[]{"-p", "strict"}).getProfile());
        assertEquals(CliOptions.ValidationProfile.STANDARD, 
            CliOptions.parse(new String[]{"--profile", "standard"}).getProfile());
        assertEquals(CliOptions.ValidationProfile.MINIMAL, 
            CliOptions.parse(new String[]{"-p", "minimal"}).getProfile());
        assertEquals(CliOptions.ValidationProfile.CUSTOM, 
            CliOptions.parse(new String[]{"-p", "custom"}).getProfile());
        
        // Test invalid profile defaults to STANDARD
        assertEquals(CliOptions.ValidationProfile.STANDARD, 
            CliOptions.parse(new String[]{"-p", "invalid"}).getProfile());
    }
    
    @Test
    void testExcludeIncludePatterns() {
        String[] args = {"-e", "target", "-e", "test", "-I", "src", "-I", "main"};
        CliOptions options = CliOptions.parse(args);
        
        assertEquals(2, options.getExcludePatterns().size());
        assertTrue(options.getExcludePatterns().contains("target"));
        assertTrue(options.getExcludePatterns().contains("test"));
        
        assertEquals(2, options.getIncludePatterns().size());
        assertTrue(options.getIncludePatterns().contains("src"));
        assertTrue(options.getIncludePatterns().contains("main"));
    }
    
    @Test
    void testProperties() {
        String[] args = {"-Djava.version=21", "-Dmaven.test.skip=true", "-Dkey=value"};
        CliOptions options = CliOptions.parse(args);
        
        assertEquals("21", options.getProperties().get("java.version"));
        assertEquals("true", options.getProperties().get("maven.test.skip"));
        assertEquals("value", options.getProperties().get("key"));
    }
    
    @Test
    void testTargetPath() {
        CliOptions options = CliOptions.parse(new String[]{"pom.xml"});
        assertEquals(Paths.get("pom.xml").toAbsolutePath(), options.getTargetPath());
        
        options = CliOptions.parse(new String[]{"/path/to/project"});
        assertEquals(Paths.get("/path/to/project").toAbsolutePath(), options.getTargetPath());
        
        options = CliOptions.parse(new String[]{"-r", ".", "-s"});
        assertEquals(Paths.get(".").toAbsolutePath(), options.getTargetPath());
    }
    
    @Test
    void testComplexCommandLine() {
        String[] args = {
            "-r", "-s", "-o", "json", "-O", "report.json",
            "-S", "warning", "-p", "strict", "-e", "target",
            "-I", "src", "--fail-fast", "--no-color",
            "-Djava.version=21", "/path/to/project"
        };
        
        CliOptions options = CliOptions.parse(args);
        
        assertTrue(options.isRecursive());
        assertTrue(options.isSummaryOnly());
        assertEquals(CliOptions.OutputFormat.JSON, options.getOutputFormat());
        assertEquals(Paths.get("report.json"), options.getOutputFile());
        assertEquals(CliOptions.SeverityLevel.WARNING, options.getSeverityLevel());
        assertEquals(CliOptions.ValidationProfile.STRICT, options.getProfile());
        assertTrue(options.getExcludePatterns().contains("target"));
        assertTrue(options.getIncludePatterns().contains("src"));
        assertTrue(options.isFailFast());
        assertTrue(options.isNoColor());
        assertEquals("21", options.getProperties().get("java.version"));
        assertEquals(Paths.get("/path/to/project").toAbsolutePath(), options.getTargetPath());
    }
    
    @Test
    void testSeverityLevelIncludes() {
        assertTrue(CliOptions.SeverityLevel.ALL.includes(CliOptions.SeverityLevel.ERROR));
        assertTrue(CliOptions.SeverityLevel.ALL.includes(CliOptions.SeverityLevel.WARNING));
        assertTrue(CliOptions.SeverityLevel.ALL.includes(CliOptions.SeverityLevel.INFO));
        
        assertTrue(CliOptions.SeverityLevel.INFO.includes(CliOptions.SeverityLevel.ERROR));
        assertTrue(CliOptions.SeverityLevel.INFO.includes(CliOptions.SeverityLevel.WARNING));
        assertFalse(CliOptions.SeverityLevel.INFO.includes(CliOptions.SeverityLevel.ALL));
        
        assertTrue(CliOptions.SeverityLevel.WARNING.includes(CliOptions.SeverityLevel.ERROR));
        assertFalse(CliOptions.SeverityLevel.WARNING.includes(CliOptions.SeverityLevel.INFO));
        
        assertFalse(CliOptions.SeverityLevel.ERROR.includes(CliOptions.SeverityLevel.WARNING));
        assertFalse(CliOptions.SeverityLevel.ERROR.includes(CliOptions.SeverityLevel.INFO));
    }
}
