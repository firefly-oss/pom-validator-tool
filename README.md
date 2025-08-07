# POM Validator Tool

A comprehensive Java 21 tool for validating Maven POM files, checking for structure issues, dependency problems, version conflicts, and best practices violations. The tool provides actionable suggestions for fixing detected issues.

## Features

- **Basic Structure Validation**: Checks for required GAV coordinates, model version, and packaging
- **Dependency Analysis**: Detects duplicate dependencies, version conflicts, and problematic dependency patterns
- **Property Validation**: Ensures standard Maven properties are defined and consistent
- **Plugin Validation**: Validates plugin configurations and checks for missing versions
- **Version Validation**: Checks version format compliance and consistency
- **Best Practices**: Enforces Maven best practices and detects common anti-patterns
- **Fix Suggestions**: Provides specific, actionable suggestions for resolving each identified issue

## Building the Project

```bash
# Compile the project
mvn compile

# Run tests
mvn test

# Build the JAR
mvn package
```

## Usage

### Command Line

```bash
# Run validation on a POM file
java -jar target/pom-validator-tool-1.0.0-SNAPSHOT.jar /path/to/pom.xml

# Example with Maven exec plugin (recommended)
mvn exec:java -Dexec.mainClass="com.catalis.tools.pomvalidator.PomValidatorApplication" \
  -Dexec.args="/path/to/pom.xml"

# Validate your own project's POM
mvn exec:java -Dexec.mainClass="com.catalis.tools.pomvalidator.PomValidatorApplication" \
  -Dexec.args="pom.xml"
```

### Output

The tool provides three types of feedback with actionable suggestions:

- **❌ ERRORS**: Critical issues that make the POM invalid
  - 💡 **Fix**: Specific instructions for resolving the error
- **⚠️ WARNINGS**: Issues that should be addressed but don't break functionality
  - 💡 **Suggestion**: Recommended actions for improvement
- **ℹ️ INFO**: Informational messages and tips
  - 💡 **Tip**: Additional guidance when applicable

## Validation Categories

### Basic Structure
- Model version compliance (4.0.0)
- Required GAV coordinates (groupId, artifactId, version)
- Valid packaging types
- Parent POM structure validation

### Dependencies
- Duplicate dependency detection
- Version conflict identification
- SNAPSHOT dependency warnings
- Deprecated version keywords (LATEST, RELEASE)
- Scope validation
- Version management consistency

### Properties
- Standard Maven properties presence
- Encoding consistency (UTF-8 recommended)
- Java version alignment
- Compiler source/target consistency

### Plugins
- Plugin version management
- Core Maven plugin configuration
- Deprecated plugin detection
- Plugin duplication checks

### Versions
- Semantic versioning compliance
- Version format validation
- SNAPSHOT consistency
- Parent/project version alignment

### Best Practices
- Project metadata completeness
- Repository security checks (HTTP vs HTTPS)
- Naming convention compliance
- Anti-pattern detection

## Example Output

The tool provides detailed feedback with suggestions:

```
=== POM Validation Results ===
File: /path/to/pom.xml
Status: INVALID

ERRORS:
  ❌ Missing groupId
     💡 Fix: Add <groupId>com.example</groupId> element to identify your organization/project

WARNINGS:
  ⚠️  Java version and compiler source mismatch: 21 vs ${java.version}
     💡 Suggestion: Set <maven.compiler.source>${java.version}</maven.compiler.source>

INFO:
  ℹ️  GAV: com.example:my-project:1.0.0-SNAPSHOT
     💡 Tip: Consider using semantic versioning for releases

Summary: 1 errors, 1 warnings, 1 info messages
```

## Development

### Project Structure

```
src/
├── main/java/com/catalis/tools/pomvalidator/
│   ├── PomValidatorApplication.java      # Main application
│   ├── model/
│   │   └── ValidationResult.java        # Result model
│   ├── service/
│   │   └── PomValidationService.java    # Main validation orchestrator
│   ├── util/
│   │   └── PomParser.java               # POM parsing utility
│   └── validator/                       # Individual validators
│       ├── PomValidator.java
│       ├── BasicStructureValidator.java
│       ├── DependencyValidator.java
│       ├── PropertyValidator.java
│       ├── PluginValidator.java
│       ├── VersionValidator.java
│       └── BestPracticesValidator.java
└── test/java/                           # Unit tests
```

### Adding New Validators

1. Create a new validator class implementing `PomValidator`
2. Add validation logic in the `validate` method
3. Register the validator in `PomValidationService`
4. Add unit tests

## Requirements

- Java 21
- Maven 3.6+

## Contributing

To add new validators:
1. Implement the `PomValidator` interface
2. Use `ValidationIssue.of(message, suggestion)` for actionable feedback
3. Register the validator in `PomValidationService`
4. Add comprehensive unit tests

## License

This project provides Maven POM validation capabilities.
