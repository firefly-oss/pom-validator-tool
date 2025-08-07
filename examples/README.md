# POM Validator Tool - Integration Examples

This directory contains practical examples of how to integrate the POM Validator Tool into your development workflow and CI/CD pipelines.

## 📁 Directory Structure

```
examples/
├── microservice-integration/        # Complete microservice CI/CD example
│   ├── .github/workflows/
│   │   └── pom-validation.yml      # GitHub Actions workflow
│   └── Dockerfile                  # Docker integration example
├── git-hooks/
│   └── pre-commit                  # Git pre-commit hook
└── README.md                       # This file
```

## 🚀 Quick Start Examples

### 1. Basic GitHub Actions Integration

Add POM validation to your existing CI pipeline:

```yaml
# Add this step to your existing workflow
- name: Validate POM
  run: |
    curl -fsSL https://raw.githubusercontent.com/firefly-oss/pom-validator-tool/main/install.sh | bash
    echo "$HOME/.local/bin" >> $GITHUB_PATH
    pom-validator pom.xml
```

### 2. Multi-Module Project Validation

For Maven multi-module projects:

```bash
# Validate all POM files in the project
find . -name "pom.xml" -type f | while read pom; do
  echo "Validating $pom"
  pom-validator "$pom"
done
```

### 3. Docker Build Integration

Add to your Dockerfile:

```dockerfile
# Validate POM during Docker build
RUN curl -fsSL https://raw.githubusercontent.com/firefly-oss/pom-validator-tool/main/install.sh | bash && \
    $HOME/.local/bin/pom-validator pom.xml
```

## 📋 Integration Scenarios

### Scenario 1: Spring Boot Microservice

Perfect for validating Spring Boot microservices in a CI/CD pipeline:

- ✅ Validates POM structure before tests
- ✅ Ensures dependency management best practices
- ✅ Catches configuration issues early
- ✅ Integrates with Docker builds

**Use the file:** `microservice-integration/.github/workflows/pom-validation.yml`

### Scenario 2: Git Pre-commit Validation

Prevent bad POMs from being committed:

- ✅ Validates only changed POM files
- ✅ Auto-installs the tool if needed
- ✅ Provides clear error messages
- ✅ Blocks commits with validation errors

**Use the file:** `git-hooks/pre-commit`

**Installation:**
```bash
# Copy to your project
cp examples/git-hooks/pre-commit .git/hooks/pre-commit
chmod +x .git/hooks/pre-commit
```

### Scenario 3: Enterprise Multi-Module Project

For large enterprise projects with multiple modules:

```yaml
# GitHub Actions example for multi-module validation
- name: Validate all POMs
  run: |
    curl -fsSL https://raw.githubusercontent.com/firefly-oss/pom-validator-tool/main/install.sh | bash
    echo "$HOME/.local/bin" >> $GITHUB_PATH
    
    # Validate parent POM first
    pom-validator pom.xml
    
    # Validate all module POMs
    find . -name "pom.xml" -path "*/src/*" -prune -o -name "pom.xml" -type f -print | \
    while read pom; do
      if [ "$pom" != "./pom.xml" ]; then
        echo "Validating module: $pom"
        pom-validator "$pom"
      fi
    done
```

## 🔧 Customization Options

### Environment Variables

Configure the tool behavior:

```bash
# Skip certain validations (if supported in future versions)
export POM_VALIDATOR_SKIP_WARNINGS=true

# Custom configuration file location
export POM_VALIDATOR_CONFIG=/path/to/config.yml
```

### Exit Code Handling

Handle validation results in scripts:

```bash
#!/bin/bash
if pom-validator pom.xml; then
  echo "✅ POM validation passed"
  # Continue with build
  mvn clean package
else
  echo "❌ POM validation failed"
  echo "Please fix the issues before proceeding"
  exit 1
fi
```

### Conditional Validation

Only validate when POM changes:

```bash
# In GitHub Actions
- name: Check if POM changed
  id: pom-changed
  run: |
    if git diff --name-only HEAD~1 HEAD | grep -q "pom.xml"; then
      echo "changed=true" >> $GITHUB_OUTPUT
    else
      echo "changed=false" >> $GITHUB_OUTPUT
    fi

- name: Validate POM
  if: steps.pom-changed.outputs.changed == 'true'
  run: pom-validator pom.xml
```

## 🏗️ CI/CD Platform Examples

### Jenkins Pipeline

```groovy
pipeline {
    agent any
    
    stages {
        stage('POM Validation') {
            steps {
                sh '''
                    curl -fsSL https://raw.githubusercontent.com/firefly-oss/pom-validator-tool/main/install.sh | bash
                    $HOME/.local/bin/pom-validator pom.xml
                '''
            }
        }
        
        stage('Build') {
            when { 
                expression { currentBuild.result != 'FAILURE' }
            }
            steps {
                sh 'mvn clean package'
            }
        }
    }
}
```

### Azure DevOps

```yaml
# azure-pipelines.yml
trigger:
- main
- develop

pool:
  vmImage: 'ubuntu-latest'

variables:
  MAVEN_CACHE_FOLDER: $(Pipeline.Workspace)/.m2/repository

steps:
- task: JavaToolInstaller@0
  inputs:
    versionSpec: '21'
    jdkArchitectureOption: 'x64'
    jdkSourceOption: 'PreInstalled'

- script: |
    curl -fsSL https://raw.githubusercontent.com/firefly-oss/pom-validator-tool/main/install.sh | bash
    echo "##vso[task.prependpath]$HOME/.local/bin"
  displayName: 'Install POM Validator'

- script: |
    pom-validator pom.xml
  displayName: 'Validate POM'

- script: |
    mvn clean test
  displayName: 'Run Tests'
```

### CircleCI

```yaml
# .circleci/config.yml
version: 2.1

jobs:
  validate-and-test:
    docker:
      - image: cimg/openjdk:21.0
    
    steps:
      - checkout
      
      - run:
          name: Install POM Validator
          command: |
            curl -fsSL https://raw.githubusercontent.com/firefly-oss/pom-validator-tool/main/install.sh | bash
            echo 'export PATH="$HOME/.local/bin:$PATH"' >> $BASH_ENV
      
      - run:
          name: Validate POM
          command: pom-validator pom.xml
      
      - run:
          name: Run Tests
          command: mvn clean test

workflows:
  main:
    jobs:
      - validate-and-test
```

## 💡 Best Practices

1. **Early Validation**: Run POM validation as the first step in your CI pipeline
2. **Fail Fast**: Configure pipelines to fail immediately on validation errors
3. **Multi-Module Support**: Validate all POMs in multi-module projects
4. **Cache Dependencies**: Cache Maven dependencies after POM validation passes
5. **Informative Messages**: Use the tool's suggestions to guide developers
6. **Pre-commit Hooks**: Catch issues before they reach the repository

## 🔍 Troubleshooting

### Common Issues

1. **Tool not found**: Ensure `$HOME/.local/bin` is in your PATH
2. **Permission denied**: Make sure the installation script has execute permissions
3. **Java not found**: Ensure Java 21+ is installed and available
4. **Network issues**: Check if curl/wget can access GitHub

### Debug Commands

```bash
# Check if tool is installed
which pom-validator

# Check Java version
java -version

# Verbose installation
bash -x install.sh

# Manual validation with full output
java -jar ~/.local/share/pom-validator-tool/pom-validator-tool.jar pom.xml
```

## 📚 Additional Resources

- [Main README](../README.md) - Complete tool documentation
- [Installation Script](../install.sh) - Installation script source
- [GitHub Repository](https://github.com/firefly-oss/pom-validator-tool) - Source code and releases

## 🤝 Contributing Examples

Have a great integration example? Please contribute!

1. Create a new directory under `examples/`
2. Add clear documentation
3. Test your example thoroughly
4. Submit a pull request

Examples we'd love to see:
- Gradle integration
- IntelliJ IDEA plugin configuration
- Maven wrapper integration
- Kubernetes deployment examples
- Terraform/Infrastructure as Code integration
