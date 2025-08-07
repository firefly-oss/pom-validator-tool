# POM Validator Tool - Complete Tutorial

## Table of Contents
1. [Introduction](#introduction)
2. [Getting Started](#getting-started)
3. [Basic Usage](#basic-usage)
4. [Advanced Features](#advanced-features)
5. [Real-World Examples](#real-world-examples)
6. [Banking Use Cases](#banking-use-cases)
7. [Troubleshooting](#troubleshooting)

## Introduction

The POM Validator Tool is part of the **Firefly OpenCore Banking Platform**, designed to ensure Maven POM files meet enterprise standards required in financial services and modern microservices architectures.

### Why POM Validation Matters in Banking

In financial services, build reproducibility and dependency management are critical:
- **Security**: Vulnerable dependencies can expose financial data
- **Compliance**: Auditors require consistent, traceable builds
- **Reliability**: Version conflicts can cause production failures
- **Modularity**: Clean module boundaries prevent service coupling

## Getting Started

### Installation

The quickest way to get started:

```bash
# Install via script
curl -fsSL https://raw.githubusercontent.com/firefly-oss/pom-validator-tool/main/install.sh | bash

# Verify installation
pom-validator --version
```

### Your First Validation

Let's validate a simple POM file:

```bash
# Navigate to your Maven project
cd /path/to/maven-project

# Run validation
pom-validator

# Or validate a specific file
pom-validator pom.xml
```

## Basic Usage

### Understanding the Output

The tool provides three levels of feedback:

#### 1. Errors (❌)
Critical issues that must be fixed:
```
❌ Missing groupId
   💡 Fix: Add <groupId>com.firefly.banking</groupId> element
```

#### 2. Warnings (⚠️)
Important issues that should be addressed:
```
⚠️  Using SNAPSHOT dependency in production
   💡 Suggestion: Use release versions for production builds
```

#### 3. Information (ℹ️)
Helpful tips and best practices:
```
ℹ️  Consider using property for version management
   💡 Tip: Define <spring.version>3.2.0</spring.version> in properties
```

### Common Validation Scenarios

#### Single Module Project
```bash
# Basic validation
pom-validator pom.xml

# Quiet mode (errors only)
pom-validator -q pom.xml

# Verbose mode (detailed output)
pom-validator -V pom.xml
```

#### Multi-Module Project
```bash
# Validate all modules recursively
pom-validator -r .

# Show summary only
pom-validator -r -s .

# Validate with specific severity
pom-validator -r -S error .
```

## Advanced Features

### 1. Watch Mode 🔍

Perfect for development - automatically validates POMs when they change:

```bash
# Watch current directory
pom-validator --watch .

# Watch recursively
pom-validator -w -r /path/to/project
```

**Example Output:**
```
🔍 POM Validator - Watch Mode
Monitoring: /Users/dev/banking-service
Mode: Recursive
Press Ctrl+C to stop watching...
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

✅ pom.xml - Valid (no issues)
✅ account-service/pom.xml - Valid (no issues)
⚠️  transaction-service/pom.xml - Valid with 2 warnings

[15:30:45] POM modified: transaction-service/pom.xml
❌ transaction-service/pom.xml - Invalid (1 errors, 2 warnings)
    └─ Duplicate dependency: org.springframework:spring-core
```

### 2. Interactive Mode 🔧

Fix issues with guided assistance:

```bash
# Start interactive mode
pom-validator --interactive pom.xml

# Or shorter
pom-validator -i .
```

**Interactive Session Example:**
```
🔧 POM Validator - Interactive Mode
File: /Users/dev/payment-gateway/pom.xml
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

Validation Summary:
  Errors: 2
  Warnings: 3
  Info: 5

Would you like to review and fix these issues? (y/n): y
✓ Backup created: pom.xml.backup

Issue 1 of 10
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

ERROR: Missing project.build.sourceEncoding property
💡 Suggestion: Add UTF-8 encoding to properties

Options:
  [f] - Apply suggested fix (if available)
  [s] - Skip this issue
  [v] - View POM section
  [e] - Edit manually
  [q] - Quit

Your choice: f
✓ Fix applied!
```

### 3. Output Formats 📊

Generate reports in different formats:

#### JSON Output
```bash
# Output to console
pom-validator -o json .

# Save to file
pom-validator -o json -O validation-report.json .
```

**Example JSON:**
```json
{
  "tool": "POM Validator Tool",
  "version": "1.0.0-SNAPSHOT",
  "timestamp": "2025-08-07T15:30:00",
  "summary": {
    "validPoms": 3,
    "invalidPoms": 1,
    "totalErrors": 2,
    "totalWarnings": 5,
    "totalInfos": 12
  },
  "results": [...]
}
```

#### Markdown Report
```bash
# Generate markdown report
pom-validator -o markdown -O VALIDATION.md -r .
```

**Example Markdown Output:**
```markdown
# POM Validation Report

**Generated by:** POM Validator Tool v1.0.0-SNAPSHOT  
**Date:** 2025-08-07T15:30:00  
**Total POMs Analyzed:** 4

## Summary

| Metric | Count |
|--------|-------|
| ✅ Valid POMs | 3 |
| ❌ Invalid POMs | 1 |
| 🔴 Total Errors | 2 |
| 🟡 Total Warnings | 5 |
```

### 4. Filtering and Profiles 🎯

#### Severity Filtering
```bash
# Show only errors
pom-validator -S error .

# Show errors and warnings
pom-validator -S warning .

# Show everything (default)
pom-validator -S all .
```

#### Validation Profiles
```bash
# Strict validation (all checks)
pom-validator -p strict .

# Standard validation (default)
pom-validator -p standard .

# Minimal validation (critical only)
pom-validator -p minimal .
```

#### Path Filtering
```bash
# Exclude test modules
pom-validator -r -e "**/test/**" .

# Include only specific modules
pom-validator -r -I "core-*" .

# Multiple excludes
pom-validator -r -e target -e node_modules -e .git .
```

### 5. CI/CD Integration 🚀

#### Fail-Fast Mode
```bash
# Stop on first error (useful in CI)
pom-validator --fail-fast -r .
```

#### Quiet Mode
```bash
# Suppress output (only exit code)
pom-validator -q .

# Check exit code
echo $?  # 0 = valid, 1 = errors found
```

## Real-World Examples

### Example 1: Banking Microservice Project

Structure:
```
banking-platform/
├── pom.xml (parent)
├── account-service/
│   └── pom.xml
├── transaction-service/
│   └── pom.xml
├── payment-gateway/
│   └── pom.xml
└── common-lib/
    └── pom.xml
```

Validation commands:
```bash
# Full validation with report
pom-validator -r -o markdown -O validation-report.md .

# Watch during development
pom-validator -w -r .

# CI pipeline validation
pom-validator -r -S error --fail-fast .

# Interactive fixing session
pom-validator -i -r .
```

### Example 2: Library Project

For a single library project:
```bash
# Basic validation
pom-validator

# Generate JSON report for documentation
pom-validator -o json -O docs/pom-validation.json .

# Pre-release check
pom-validator -p strict .
```

### Example 3: Legacy Project Migration

When modernizing a legacy project:
```bash
# Start with minimal checks
pom-validator -p minimal .

# Gradually increase strictness
pom-validator -p standard .

# Finally, apply strict validation
pom-validator -p strict .

# Fix issues interactively
pom-validator -i .
```

## Banking Use Cases

### 1. Compliance Validation

Ensure POMs meet regulatory requirements:

```bash
# Check for reproducible builds
pom-validator -p strict --output json -O compliance-report.json .

# Verify no SNAPSHOT dependencies in production
pom-validator -S error . | grep -i snapshot
```

### 2. Security Scanning

Identify potentially vulnerable configurations:

```bash
# Check for HTTP repositories (should use HTTPS)
pom-validator . | grep -i "http://"

# Verify dependency versions are specified
pom-validator -S warning . | grep -i "version"
```

### 3. Multi-Team Coordination

For large banking platforms with multiple teams:

```bash
# Generate team-specific reports
for team in payments accounts transactions; do
  pom-validator -r -o markdown -O reports/${team}-validation.md ${team}-service/
done

# Aggregate results
pom-validator -r -s -o json -O aggregate-report.json .
```

### 4. Release Preparation

Pre-release validation checklist:

```bash
#!/bin/bash
# release-check.sh

echo "🔍 Running release validation checks..."

# 1. No SNAPSHOT dependencies
echo "Checking for SNAPSHOT dependencies..."
if pom-validator -S error . | grep -q "SNAPSHOT"; then
  echo "❌ Found SNAPSHOT dependencies"
  exit 1
fi

# 2. Strict validation must pass
echo "Running strict validation..."
if ! pom-validator -p strict -q .; then
  echo "❌ Strict validation failed"
  exit 1
fi

# 3. Generate compliance report
echo "Generating compliance report..."
pom-validator -o json -O release-validation.json .

echo "✅ All release checks passed!"
```

## Troubleshooting

### Common Issues and Solutions

#### 1. "POM file not found"
```bash
# Ensure you're in the right directory
pwd

# Check if pom.xml exists
ls -la pom.xml

# Specify path explicitly
pom-validator ./pom.xml
```

#### 2. Colors not displaying correctly
```bash
# Disable colors for terminals that don't support ANSI
pom-validator --no-color .

# Set TERM environment variable
TERM=xterm-256color pom-validator .
```

#### 3. Permission denied errors
```bash
# Ensure the tool is executable
chmod +x ~/.local/bin/pom-validator

# Run with explicit Java
java -jar ~/.local/lib/pom-validator.jar .
```

#### 4. Out of memory for large projects
```bash
# Increase Java heap size
JAVA_OPTS="-Xmx2g" pom-validator -r .

# Or modify the wrapper script
export JAVA_OPTS="-Xmx2g"
pom-validator -r .
```

### Debug Mode

For detailed troubleshooting:
```bash
# Enable verbose output
pom-validator -V .

# Enable debug logging (if implemented)
pom-validator -Ddebug=true .

# Check Java version
java -version
```

## Best Practices

### 1. Regular Validation

Integrate validation into your workflow:

```bash
# Add to git pre-commit hook
echo 'pom-validator -q .' >> .git/hooks/pre-commit
chmod +x .git/hooks/pre-commit
```

### 2. Progressive Enhancement

Start with minimal validation and gradually increase:

```bash
# Week 1: Fix errors only
pom-validator -S error .

# Week 2: Address warnings
pom-validator -S warning .

# Week 3: Apply all recommendations
pom-validator -S all .
```

### 3. Team Standards

Create a team configuration:

```bash
# .pomvalidatorrc (coming soon)
{
  "profile": "strict",
  "severity": "warning",
  "exclude": ["**/test/**", "**/target/**"],
  "output": "markdown",
  "outputFile": "validation-report.md"
}
```

### 4. Documentation

Keep validation reports in your repository:

```bash
# Add to your build process
mvn clean compile
pom-validator -o markdown -O docs/pom-validation.md .
git add docs/pom-validation.md
git commit -m "Update POM validation report"
```

## Conclusion

The POM Validator Tool is an essential component of the Firefly OpenCore Banking Platform, helping ensure that Maven projects meet the high standards required in financial services. By integrating it into your development workflow, you can:

- **Prevent Issues Early**: Catch problems before they reach production
- **Maintain Consistency**: Ensure all projects follow the same standards
- **Improve Security**: Identify vulnerable dependencies and configurations
- **Streamline Development**: Fix issues quickly with interactive mode
- **Meet Compliance**: Generate reports for auditors and compliance teams

For more information, visit the [Firefly OpenCore Banking Platform](https://github.com/firefly-oss) repository.

---

**Need Help?**
- 📖 [Documentation](https://github.com/firefly-oss/pom-validator-tool)
- 🐛 [Report Issues](https://github.com/firefly-oss/pom-validator-tool/issues)
- 💬 [Discussions](https://github.com/firefly-oss/pom-validator-tool/discussions)
- 📧 [Security Issues](mailto:security@firefly-platform.com)
