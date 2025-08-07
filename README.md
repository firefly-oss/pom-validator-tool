# POM Validator Tool

A comprehensive Java 21 tool for validating Maven POM files, checking for structure issues, dependency problems, version conflicts, and best practices violations.

## Features

- **Basic Structure Validation**: Checks for required GAV coordinates, model version, and packaging
- **Dependency Analysis**: Detects duplicate dependencies, version conflicts, and problematic dependency patterns
- **Property Validation**: Ensures standard Maven properties are defined and consistent
- **Plugin Validation**: Validates plugin configurations and checks for missing versions
- **Version Validation**: Checks version format compliance and consistency
- **Best Practices**: Enforces Maven best practices and detects common anti-patterns

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

# Example with the parent POM
java -jar target/pom-validator-tool-1.0.0-SNAPSHOT.jar $HOME/Development/lib-parent-pom/pom.xml
```

### Output

The tool provides three types of feedback:

- **❌ ERRORS**: Critical issues that make the POM invalid
- **⚠️ WARNINGS**: Issues that should be addressed but don't break functionality  
- **ℹ️ INFO**: Informational messages and suggestions

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

## Testing with Parent POM

To test the tool with the provided parent POM:

```bash
# Build the tool
mvn clean package

# Test with parent POM
java -jar target/pom-validator-tool-1.0.0-SNAPSHOT.jar $HOME/Development/lib-parent-pom/pom.xml
```

Expected validations for the parent POM:
- ✅ Well-structured parent POM with proper dependency management
- ✅ Java 21 configuration
- ✅ Comprehensive property definitions
- ✅ Plugin management setup
- ℹ️ Informational messages about Spring Boot and Maven configuration

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

## License

This project is part of the Catalis development tools suite.
