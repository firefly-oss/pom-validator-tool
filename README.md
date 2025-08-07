# POM Validator Tool

<p align="center">
  <img src="https://img.shields.io/badge/Firefly%20Platform-Toolset-orange?style=for-the-badge&logo=fire" alt="Firefly Platform Toolset">
  <img src="https://img.shields.io/badge/Java-21-blue?style=for-the-badge&logo=openjdk" alt="Java 21">
  <img src="https://img.shields.io/badge/Maven-3.6+-red?style=for-the-badge&logo=apache-maven" alt="Maven 3.6+">
  <img src="https://img.shields.io/badge/License-Apache%202.0-green?style=for-the-badge" alt="Apache 2.0 License">
</p>

> **Part of the [Firefly OpenCore Banking Platform](https://github.com/firefly-oss)** - An open-source core banking platform providing enterprise-grade financial services infrastructure and developer tools.

A comprehensive Java 21 tool for validating Maven POM files, checking for structure issues, dependency problems, version conflicts, and best practices violations. The tool provides actionable suggestions for fixing detected issues.

## 🔥 About Firefly OpenCore Banking Platform

**Firefly** is an **OpenCore Banking Platform** that provides modern, cloud-native infrastructure for financial services. As part of our commitment to the open-source community, we're releasing a suite of development tools that we use internally to maintain code quality and consistency across our microservices architecture.

This POM Validator Tool is part of the **Firefly Platform Toolset** - a collection of enterprise-grade tools we're open-sourcing to help developers build robust, scalable financial applications.

### Platform Integration

- **🏦 Banking Microservices**: Ensures POM consistency across financial service modules
- **🏗️ CI/CD Pipeline**: Integrates with banking platform build pipelines
- **📊 Compliance & Quality**: Enforces enterprise standards required in financial services
- **🔧 Developer Experience**: Streamlines development of banking applications
- **🚀 Cloud-Native Ready**: Optimized for containerized banking microservices

## Features

- **Basic Structure Validation**: Checks for required GAV coordinates, model version, and packaging
- **Dependency Analysis**: Detects duplicate dependencies, version conflicts, and problematic dependency patterns
- **Property Validation**: Ensures standard Maven properties are defined and consistent
- **Plugin Validation**: Validates plugin configurations and checks for missing versions
- **Multi-Module Support**: Validates parent-child relationships and module references
- **Version Validation**: Checks version format compliance and consistency
- **Best Practices**: Enforces Maven best practices and detects common anti-patterns
- **Fix Suggestions**: Provides specific, actionable suggestions for resolving each identified issue
- **Recursive Validation**: Validates entire multi-module project hierarchies
- **Summary Reports**: Aggregated statistics for multi-module projects

## Installation

### Quick Install (Recommended)

```bash
# Download and run the installation script
curl -fsSL https://raw.githubusercontent.com/firefly-oss/pom-validator-tool/main/install.sh | bash

# Or with wget
wget -qO- https://raw.githubusercontent.com/firefly-oss/pom-validator-tool/main/install.sh | bash
```

### Manual Installation

1. **Download the installation script:**
   ```bash
   curl -O https://raw.githubusercontent.com/firefly-oss/pom-validator-tool/main/install.sh
   chmod +x install.sh
   ```

2. **Run the installer:**
   ```bash
   ./install.sh
   ```

3. **Install from source (requires Maven):**
   ```bash
   ./install.sh --source
   ```

### Manual Build

If you prefer to build manually:

```bash
# Clone the repository
git clone https://github.com/firefly-oss/pom-validator-tool.git
cd pom-validator-tool

# Compile the project
mvn compile

# Run tests
mvn test

# Build the JAR (creates an executable fat JAR with all dependencies)
mvn package

# The executable JAR will be created at:
# target/pom-validator-tool-1.0.0-SNAPSHOT.jar
```

### Prerequisites

- Java 21 or higher
- Maven 3.6+ (only for building from source)
- curl or wget (for installation script)

## Usage

### Command Line

After installation, use the `pom-validator` command:

```bash
# Show help and usage information
pom-validator --help
pom-validator -h

# Validate current directory's pom.xml
pom-validator
pom-validator .

# Validate a specific POM file
pom-validator /path/to/pom.xml

# Validate a multi-module project (all modules)
pom-validator --recursive /path/to/project
pom-validator -r .

# Show only summary for multi-module validation
pom-validator -r -s /path/to/project

# Show version information
pom-validator --version
```

### Command Line Options

#### Basic Options
| Option | Short | Description |
|--------|-------|-------------|
| `--help` | `-h` | Show comprehensive help message with examples |
| `--version` | `-v` | Show version information |
| `--recursive` | `-r` | Validate all POM files recursively in multi-module projects |
| `--summary` | `-s` | Show only summary (no detailed messages) |

#### Advanced Modes
| Option | Short | Description |
|--------|-------|-------------|
| `--watch` | `-w` | Watch mode - continuously monitor POM files for changes |
| `--interactive` | `-i` | Interactive mode - fix issues with guided assistance |
| `--auto-fix` | `-f` | Automatically fix common issues (coming soon) |

#### Output Options
| Option | Short | Description |
|--------|-------|-------------|
| `--output <format>` | `-o` | Output format: `console`, `json`, `markdown`, `xml`, `html`, `junit` |
| `--output-file <path>` | `-O` | Write output to specified file |
| `--quiet` | `-q` | Suppress all output |
| `--verbose` | `-V` | Show detailed output |
| `--no-color` | | Disable colored output |

#### Filtering Options
| Option | Short | Description |
|--------|-------|-------------|
| `--severity <level>` | `-S` | Minimum severity level: `error`, `warning`, `info`, `all` |
| `--profile <name>` | `-p` | Validation profile: `strict`, `standard`, `minimal` |
| `--exclude <pattern>` | `-e` | Exclude files matching pattern |
| `--include <pattern>` | `-I` | Include only files matching pattern |
| `--fail-fast` | | Stop validation on first error |

#### Configuration
| Option | Short | Description |
|--------|-------|-------------|
| `--config <file>` | `-c` | Use configuration file |
| `-D<prop>=<value>` | | Set a property value |

### Direct JAR Usage

If you built manually or want to use the JAR directly:

```bash
# Run validation on a POM file
java -jar target/pom-validator-tool-1.0.0-SNAPSHOT.jar /path/to/pom.xml

# Example with Maven exec plugin
mvn exec:java -Dexec.mainClass="com.catalis.tools.pomvalidator.PomValidatorApplication" \
  -Dexec.args="/path/to/pom.xml"
```

### Exit Codes

- `0`: POM is valid (no errors found)
- `1`: POM has errors or validation failed

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

### Multi-Module Projects
- Parent POM validation (packaging type, module references)
- Child POM validation (parent references, relative paths)
- Module directory existence checks
- Version inheritance validation
- Dependency management inheritance

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

## CI/CD Integration

### GitHub Actions

Integrate POM validation into your CI pipeline:

#### Basic Validation

```yaml
name: POM Validation

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main ]

jobs:
  validate-pom:
    runs-on: ubuntu-latest
    
    steps:
    - name: Checkout code
      uses: actions/checkout@v4
      
    - name: Set up Java
      uses: actions/setup-java@v4
      with:
        java-version: '21'
        distribution: 'temurin'
        
    - name: Install POM Validator
      run: |
        curl -fsSL https://raw.githubusercontent.com/firefly-oss/pom-validator-tool/main/install.sh | bash
        echo "$HOME/.local/bin" >> $GITHUB_PATH
        
    - name: Validate POM
      run: pom-validator pom.xml
```

#### Multi-Module Project Validation

```yaml
name: Multi-Module POM Validation

on: [push, pull_request]

jobs:
  validate-poms:
    runs-on: ubuntu-latest
    
    steps:
    - name: Checkout code
      uses: actions/checkout@v4
      
    - name: Set up Java
      uses: actions/setup-java@v4
      with:
        java-version: '21'
        distribution: 'temurin'
        
    - name: Install POM Validator
      run: |
        curl -fsSL https://raw.githubusercontent.com/firefly-oss/pom-validator-tool/main/install.sh | bash
        echo "$HOME/.local/bin" >> $GITHUB_PATH
        
    - name: Find and validate all POMs
      run: |
        find . -name "pom.xml" -type f | while read pom; do
          echo "Validating $pom"
          if ! pom-validator "$pom"; then
            echo "❌ Validation failed for $pom"
            exit 1
          fi
        done
```

#### Microservice Pipeline Example

```yaml
name: Microservice CI/CD

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main ]

jobs:
  quality-checks:
    name: Quality Checks
    runs-on: ubuntu-latest
    
    steps:
    - name: Checkout code
      uses: actions/checkout@v4
      
    - name: Set up Java
      uses: actions/setup-java@v4
      with:
        java-version: '21'
        distribution: 'temurin'
        
    - name: Cache Maven dependencies
      uses: actions/cache@v3
      with:
        path: ~/.m2
        key: ${{ runner.os }}-m2-${{ hashFiles('**/pom.xml') }}
        restore-keys: ${{ runner.os }}-m2
        
    - name: Install POM Validator
      run: |
        curl -fsSL https://raw.githubusercontent.com/firefly-oss/pom-validator-tool/main/install.sh | bash
        echo "$HOME/.local/bin" >> $GITHUB_PATH
        
    - name: Validate POM structure
      run: |
        echo "🔍 Validating POM structure and dependencies..."
        pom-validator pom.xml
        
    - name: Run Maven tests
      run: mvn clean test
      
    - name: Run Maven verify
      run: mvn verify
      
  build:
    name: Build and Package
    needs: quality-checks
    runs-on: ubuntu-latest
    if: github.ref == 'refs/heads/main'
    
    steps:
    - name: Checkout code
      uses: actions/checkout@v4
      
    - name: Set up Java
      uses: actions/setup-java@v4
      with:
        java-version: '21'
        distribution: 'temurin'
        
    - name: Build application
      run: mvn clean package -DskipTests
      
    - name: Build Docker image
      run: |
        docker build -t myapp:${{ github.sha }} .
        docker tag myapp:${{ github.sha }} myapp:latest
```

### Jenkins Pipeline

```groovy
pipeline {
    agent any
    
    tools {
        maven 'Maven-3.9'
        jdk 'OpenJDK-21'
    }
    
    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }
        
        stage('Install POM Validator') {
            steps {
                sh '''
                    curl -fsSL https://raw.githubusercontent.com/firefly-oss/pom-validator-tool/main/install.sh | bash
                    export PATH="$HOME/.local/bin:$PATH"
                '''
            }
        }
        
        stage('Validate POM') {
            steps {
                sh '$HOME/.local/bin/pom-validator pom.xml'
            }
        }
        
        stage('Test') {
            steps {
                sh 'mvn clean test'
            }
            post {
                always {
                    junit 'target/surefire-reports/*.xml'
                }
            }
        }
        
        stage('Package') {
            steps {
                sh 'mvn package -DskipTests'
            }
            post {
                success {
                    archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
                }
            }
        }
    }
}
```

### GitLab CI

```yaml
stages:
  - validate
  - test
  - build

variables:
  MAVEN_OPTS: "-Dmaven.repo.local=.m2/repository"
  
cache:
  paths:
    - .m2/repository/
    
validate-pom:
  stage: validate
  image: openjdk:21-jdk
  before_script:
    - apt-get update -qq && apt-get install -y -qq curl
    - curl -fsSL https://raw.githubusercontent.com/firefly-oss/pom-validator-tool/main/install.sh | bash
    - export PATH="$HOME/.local/bin:$PATH"
  script:
    - $HOME/.local/bin/pom-validator pom.xml
  rules:
    - if: $CI_PIPELINE_SOURCE == "push"
    - if: $CI_PIPELINE_SOURCE == "merge_request_event"
    
test:
  stage: test
  image: maven:3.9-openjdk-21
  script:
    - mvn clean test
  artifacts:
    reports:
      junit:
        - target/surefire-reports/TEST-*.xml
        
build:
  stage: build
  image: maven:3.9-openjdk-21
  script:
    - mvn package -DskipTests
  artifacts:
    paths:
      - target/*.jar
    expire_in: 1 day
  only:
    - main
```

### Pre-commit Hook

Add POM validation to your pre-commit hooks:

```bash
#!/bin/bash
# .git/hooks/pre-commit

# Check if POM files are being committed
if git diff --cached --name-only | grep -q "pom\.xml$"; then
    echo "🔍 Validating POM files..."
    
    # Find all staged POM files and validate them
    for pom in $(git diff --cached --name-only | grep "pom\.xml$"); do
        if [ -f "$pom" ]; then
            echo "Validating $pom"
            if ! pom-validator "$pom"; then
                echo "❌ POM validation failed for $pom"
                echo "Please fix the issues before committing."
                exit 1
            fi
        fi
    done
    
    echo "✅ All POM files validated successfully"
fi

exit 0
```

Make it executable:
```bash
chmod +x .git/hooks/pre-commit
```

## Contributing

To add new validators:
1. Implement the `PomValidator` interface
2. Use `ValidationIssue.of(message, suggestion)` for actionable feedback
3. Register the validator in `PomValidationService`
4. Add comprehensive unit tests

## 🏦 Firefly OpenCore Banking Platform

This tool is part of the **Firefly OpenCore Banking Platform** - an open-source initiative to provide modern, secure, and scalable banking infrastructure.

### Why Open Source?

As a fintech platform, we believe in:
- **Transparency**: Open-source tools build trust in financial services
- **Community**: Collaborative development improves security and reliability
- **Innovation**: Shared tools accelerate fintech innovation
- **Standards**: Common tooling promotes industry best practices

### Firefly Platform Components

#### Core Banking Services (Coming Soon)
- **Account Management Service**: Core account operations
- **Transaction Processing Engine**: High-performance transaction handling
- **Payment Gateway**: Multi-channel payment processing
- **Compliance Engine**: Regulatory compliance automation

#### Developer Tools (Open Source)
- **POM Validator Tool**: Maven project validation (this tool)
- **API Contract Validator**: OpenAPI specification validation (coming soon)
- **Security Scanner**: Automated security vulnerability detection (coming soon)
- **Performance Profiler**: Microservice performance analysis (coming soon)

### Use Cases in Banking

This POM Validator Tool helps ensure:
- **Dependency Security**: Critical for financial applications
- **Version Consistency**: Essential for regulatory compliance
- **Build Reproducibility**: Required for audit trails
- **Module Isolation**: Important for service boundaries in banking systems

### Community & Support

- **GitHub Organization**: [firefly-oss](https://github.com/firefly-oss)
- **Platform Documentation**: [Firefly Docs](https://github.com/firefly-oss/documentation) (coming soon)
- **Issues & Feature Requests**: [GitHub Issues](https://github.com/firefly-oss/pom-validator-tool/issues)
- **Discussions**: [GitHub Discussions](https://github.com/firefly-oss/pom-validator-tool/discussions)
- **Security**: Report security issues to security@firefly-platform.com

## License

This project is licensed under the Apache License, Version 2.0.

See the [LICENSE](LICENSE) file for details.

---

<p align="center">
  <strong>🔥 Firefly Platform Toolset</strong><br>
  <em>Enterprise-grade tools for modern Java development</em><br><br>
  <a href="https://github.com/firefly-oss">🌟 Explore More Tools</a> |
  <a href="https://github.com/firefly-oss/pom-validator-tool/issues">🐛 Report Issues</a> |
  <a href="https://github.com/firefly-oss/pom-validator-tool/discussions">💬 Join Discussions</a>
</p>
