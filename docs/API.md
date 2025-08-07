# API Documentation

## Table of Contents
- [Overview](#overview)
- [Core Classes](#core-classes)
- [Validators](#validators)
- [Features](#features)
- [Output Formatters](#output-formatters)
- [Usage Examples](#usage-examples)

## Overview

The POM Validator Tool can be used programmatically as a library in your Java applications. This document describes the public API.

## Core Classes

### PomValidationService

Main service class for validating POM files.

```java
import com.catalis.tools.pomvalidator.service.PomValidationService;
import com.catalis.tools.pomvalidator.model.ValidationResult;
import java.nio.file.Path;

// Create service instance
PomValidationService service = new PomValidationService();

// Validate a POM file
Path pomPath = Path.of("pom.xml");
ValidationResult result = service.validatePom(pomPath);

// Check if valid
if (result.isValid()) {
    System.out.println("POM is valid!");
} else {
    System.out.println("Errors: " + result.getErrors().size());
}
```

### ValidationResult

Container for validation results.

```java
public class ValidationResult {
    List<ValidationIssue> getErrors();
    List<ValidationIssue> getWarnings();
    List<ValidationIssue> getInfos();
    boolean isValid();
    int getTotalIssues();
    
    // Builder pattern
    static Builder builder();
}
```

### ValidationIssue

Represents a single validation issue.

```java
public class ValidationIssue {
    String getMessage();
    String getSuggestion();
    boolean hasSuggestion();
    
    // Factory method
    static ValidationIssue of(String message, String suggestion);
}
```

## Validators

### PomValidator Interface

All validators implement this interface:

```java
public interface PomValidator {
    ValidationResult validate(Model model, Path pomPath);
}
```

### Available Validators

#### BasicStructureValidator
Validates basic POM structure.

```java
PomValidator validator = new BasicStructureValidator();
ValidationResult result = validator.validate(model, pomPath);
```

Checks:
- Model version (4.0.0)
- Required GAV coordinates
- Valid packaging types
- Parent POM structure

#### DependencyValidator
Validates dependencies and dependency management.

```java
PomValidator validator = new DependencyValidator();
```

Checks:
- Duplicate dependencies
- Version conflicts
- SNAPSHOT dependencies
- Deprecated version keywords
- Scope validation

#### PropertyValidator
Validates Maven properties.

```java
PomValidator validator = new PropertyValidator();
```

Checks:
- Standard Maven properties
- Encoding consistency
- Java version alignment
- Compiler configuration

#### PluginValidator
Validates plugins and plugin management.

```java
PomValidator validator = new PluginValidator();
```

Checks:
- Plugin versions
- Core Maven plugins
- Deprecated plugins
- Plugin duplication

#### MultiModuleValidator
Validates multi-module project structure.

```java
PomValidator validator = new MultiModuleValidator();
```

Checks:
- Parent-child relationships
- Module references
- Relative paths
- Version inheritance

## Features

### InteractiveMode

Interactive fixing of validation issues.

```java
import com.catalis.tools.pomvalidator.feature.InteractiveMode;

InteractiveMode interactive = new InteractiveMode();
interactive.runInteractive(pomPath);
```

### AutoFixMode

Automatic fixing of common issues.

```java
import com.catalis.tools.pomvalidator.feature.AutoFixMode;

AutoFixMode autoFix = new AutoFixMode();
autoFix.runAutoFix(pomPath, true); // true = create backup
```

### WatchMode

Monitor POM files for changes.

```java
import com.catalis.tools.pomvalidator.feature.WatchMode;

WatchMode watchMode = new WatchMode();
watchMode.watch(rootPath, true); // true = recursive
```

## Output Formatters

### OutputFormatter Interface

```java
public interface OutputFormatter {
    String format(Map<Path, ValidationResult> results);
    void write(Map<Path, ValidationResult> results, Path outputFile) throws IOException;
}
```

### JsonFormatter

Format results as JSON.

```java
import com.catalis.tools.pomvalidator.feature.formatter.JsonFormatter;

OutputFormatter formatter = new JsonFormatter();
String json = formatter.format(results);
formatter.write(results, Path.of("report.json"));
```

### MarkdownFormatter

Format results as Markdown.

```java
import com.catalis.tools.pomvalidator.feature.formatter.MarkdownFormatter;

OutputFormatter formatter = new MarkdownFormatter();
String markdown = formatter.format(results);
formatter.write(results, Path.of("report.md"));
```

## Usage Examples

### Basic Validation

```java
import com.catalis.tools.pomvalidator.service.PomValidationService;
import com.catalis.tools.pomvalidator.model.ValidationResult;
import java.nio.file.Path;

public class Example {
    public static void main(String[] args) throws Exception {
        PomValidationService service = new PomValidationService();
        Path pomPath = Path.of("pom.xml");
        
        ValidationResult result = service.validatePom(pomPath);
        
        // Process errors
        result.getErrors().forEach(error -> {
            System.err.println("ERROR: " + error.getMessage());
            if (error.hasSuggestion()) {
                System.err.println("  FIX: " + error.getSuggestion());
            }
        });
        
        // Process warnings
        result.getWarnings().forEach(warning -> {
            System.out.println("WARNING: " + warning.getMessage());
        });
        
        // Exit with appropriate code
        System.exit(result.isValid() ? 0 : 1);
    }
}
```

### Multi-Module Validation

```java
import com.catalis.tools.pomvalidator.service.PomValidationService;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

public class MultiModuleExample {
    public static void main(String[] args) throws Exception {
        PomValidationService service = new PomValidationService();
        Path rootPath = Path.of(".");
        Map<Path, ValidationResult> results = new HashMap<>();
        
        // Find all POMs
        Files.walkFileTree(rootPath, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                if (file.getFileName().toString().equals("pom.xml")) {
                    try {
                        ValidationResult result = service.validatePom(file);
                        results.put(file, result);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                return FileVisitResult.CONTINUE;
            }
        });
        
        // Generate report
        JsonFormatter formatter = new JsonFormatter();
        formatter.write(results, Path.of("validation-report.json"));
    }
}
```

### Custom Validator

```java
import com.catalis.tools.pomvalidator.validator.PomValidator;
import com.catalis.tools.pomvalidator.model.*;
import org.apache.maven.model.Model;
import java.nio.file.Path;

public class CustomValidator implements PomValidator {
    @Override
    public ValidationResult validate(Model model, Path pomPath) {
        ValidationResult result = new ValidationResult();
        
        // Custom validation logic
        if (model.getName() == null || model.getName().isEmpty()) {
            result.addWarning(ValidationIssue.of(
                "Project name is missing",
                "Add <name>Your Project Name</name>"
            ));
        }
        
        // Check custom property
        if (model.getProperties() != null) {
            String customProp = model.getProperties().getProperty("custom.property");
            if (customProp == null) {
                result.addInfo(ValidationIssue.of(
                    "Consider adding custom.property",
                    "Define <custom.property>value</custom.property> in properties"
                ));
            }
        }
        
        return result;
    }
}
```

### Integration with Build Tools

#### Maven Plugin Usage

```xml
<plugin>
    <groupId>com.catalis.tools</groupId>
    <artifactId>pom-validator-maven-plugin</artifactId>
    <version>1.0.0</version>
    <executions>
        <execution>
            <phase>validate</phase>
            <goals>
                <goal>validate</goal>
            </goals>
            <configuration>
                <failOnError>true</failOnError>
                <profile>strict</profile>
            </configuration>
        </execution>
    </executions>
</plugin>
```

#### Gradle Plugin Usage

```groovy
plugins {
    id 'com.catalis.tools.pom-validator' version '1.0.0'
}

pomValidator {
    profile = 'strict'
    failOnError = true
    outputFormat = 'json'
    outputFile = file('build/reports/pom-validation.json')
}
```

### Programmatic CLI Options

```java
import com.catalis.tools.pomvalidator.cli.CliOptions;

// Parse command line arguments
CliOptions options = CliOptions.parse(args);

// Check options
if (options.isWatch()) {
    // Run watch mode
}

if (options.getOutputFormat() == CliOptions.OutputFormat.JSON) {
    // Use JSON formatter
}

// Access severity level
CliOptions.SeverityLevel severity = options.getSeverityLevel();

// Access validation profile
CliOptions.ValidationProfile profile = options.getProfile();
```

## Error Codes

The tool uses standard exit codes:

- `0` - Validation successful (no errors)
- `1` - Validation failed (errors found)
- `2` - Tool error (e.g., file not found)

## Thread Safety

- `PomValidationService` is thread-safe and can be shared
- `ValidationResult` is immutable once created
- Validators are stateless and thread-safe

## Performance Considerations

- Validators are lightweight and stateless
- POM parsing is cached within a validation session
- File I/O is the main bottleneck
- Use parallel processing for large projects

## Extension Points

### Custom Output Formatter

```java
public class CustomFormatter implements OutputFormatter {
    @Override
    public String format(Map<Path, ValidationResult> results) {
        // Custom formatting logic
        return customOutput;
    }
    
    @Override
    public void write(Map<Path, ValidationResult> results, Path outputFile) {
        String output = format(results);
        Files.writeString(outputFile, output);
    }
}
```

### Custom Fix Provider

```java
public interface FixProvider {
    boolean canFix(ValidationIssue issue);
    boolean applyFix(Path pomPath, ValidationIssue issue);
}
```

---

**Firefly OpenCore Banking Platform** - Building the future of financial services.
