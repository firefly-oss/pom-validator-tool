# Installation Guide

## Table of Contents
- [Prerequisites](#prerequisites)
- [Quick Installation](#quick-installation)
- [Manual Installation](#manual-installation)
- [Building from Source](#building-from-source)
- [Docker Installation](#docker-installation)
- [Verification](#verification)
- [Troubleshooting](#troubleshooting)

## Prerequisites

### Required
- **Java 21** or higher
- **Operating System**: Linux, macOS, or Windows (with WSL)

### Optional
- **Maven 3.6+** (only for building from source)
- **Git** (for cloning the repository)
- **Docker** (for containerized deployment)

## Quick Installation

### Using Installation Script (Recommended)

```bash
# Download and run the installation script
curl -fsSL https://raw.githubusercontent.com/firefly-oss/pom-validator-tool/main/install.sh | bash

# Or with wget
wget -qO- https://raw.githubusercontent.com/firefly-oss/pom-validator-tool/main/install.sh | bash
```

The script will:
1. Download the latest release
2. Install to `~/.local/bin/pom-validator`
3. Add to your PATH (if needed)
4. Verify the installation

### Homebrew (macOS/Linux)

```bash
# Coming soon
brew tap firefly-oss/tools
brew install pom-validator
```

## Manual Installation

### Step 1: Download the JAR

```bash
# Create installation directory
mkdir -p ~/.local/lib

# Download the latest JAR
curl -L https://github.com/firefly-oss/pom-validator-tool/releases/latest/download/pom-validator-tool.jar \
  -o ~/.local/lib/pom-validator-tool.jar
```

### Step 2: Create Wrapper Script

```bash
# Create bin directory
mkdir -p ~/.local/bin

# Create wrapper script
cat > ~/.local/bin/pom-validator << 'EOF'
#!/bin/bash
java -jar ~/.local/lib/pom-validator-tool.jar "$@"
EOF

# Make it executable
chmod +x ~/.local/bin/pom-validator
```

### Step 3: Add to PATH

```bash
# For bash
echo 'export PATH="$HOME/.local/bin:$PATH"' >> ~/.bashrc
source ~/.bashrc

# For zsh
echo 'export PATH="$HOME/.local/bin:$PATH"' >> ~/.zshrc
source ~/.zshrc
```

## Building from Source

### Clone the Repository

```bash
git clone https://github.com/firefly-oss/pom-validator-tool.git
cd pom-validator-tool
```

### Build with Maven

```bash
# Compile the project
mvn clean compile

# Run tests
mvn test

# Package the JAR
mvn package
```

### Install Locally

```bash
# Copy JAR to local lib
cp target/pom-validator-tool-*.jar ~/.local/lib/pom-validator-tool.jar

# Create wrapper script (see Manual Installation)
```

## Docker Installation

### Using Pre-built Image

```bash
# Pull the image
docker pull ghcr.io/firefly-oss/pom-validator:latest

# Create alias for easy usage
alias pom-validator='docker run --rm -v $(pwd):/workspace ghcr.io/firefly-oss/pom-validator'
```

### Building Docker Image Locally

```bash
# Clone repository
git clone https://github.com/firefly-oss/pom-validator-tool.git
cd pom-validator-tool

# Build image
docker build -t pom-validator .

# Run container
docker run --rm -v $(pwd):/workspace pom-validator /workspace/pom.xml
```

## Verification

### Check Installation

```bash
# Verify the command is available
which pom-validator

# Check version
pom-validator --version

# Run help
pom-validator --help
```

### Test Validation

```bash
# Create test POM
cat > test-pom.xml << EOF
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0">
    <modelVersion>4.0.0</modelVersion>
    <groupId>com.test</groupId>
    <artifactId>test</artifactId>
    <version>1.0.0</version>
</project>
EOF

# Validate
pom-validator test-pom.xml

# Clean up
rm test-pom.xml
```

## Platform-Specific Instructions

### Windows

#### Using WSL (Recommended)
```bash
# Install WSL if not already installed
wsl --install

# Follow Linux installation instructions
```

#### Using PowerShell
```powershell
# Download JAR
Invoke-WebRequest -Uri "https://github.com/firefly-oss/pom-validator-tool/releases/latest/download/pom-validator-tool.jar" `
  -OutFile "$env:USERPROFILE\.local\lib\pom-validator-tool.jar"

# Create wrapper script
@"
@echo off
java -jar %USERPROFILE%\.local\lib\pom-validator-tool.jar %*
"@ | Out-File -FilePath "$env:USERPROFILE\.local\bin\pom-validator.bat" -Encoding ASCII

# Add to PATH via System Properties
```

### Linux Package Managers

#### Debian/Ubuntu (Coming Soon)
```bash
# Add repository
sudo add-apt-repository ppa:firefly-oss/tools
sudo apt update

# Install
sudo apt install pom-validator
```

#### RHEL/CentOS/Fedora (Coming Soon)
```bash
# Add repository
sudo dnf config-manager --add-repo https://firefly-oss.github.io/rpm/firefly.repo

# Install
sudo dnf install pom-validator
```

## Troubleshooting

### Common Issues

#### "Command not found"
```bash
# Check if ~/.local/bin is in PATH
echo $PATH

# Add to PATH if missing
export PATH="$HOME/.local/bin:$PATH"
```

#### "Java not found"
```bash
# Check Java installation
java --version

# Install Java 21 if missing
# macOS
brew install openjdk@21

# Ubuntu/Debian
sudo apt install openjdk-21-jdk

# RHEL/CentOS
sudo dnf install java-21-openjdk
```

#### Permission Denied
```bash
# Make script executable
chmod +x ~/.local/bin/pom-validator

# Check file permissions
ls -la ~/.local/bin/pom-validator
```

#### Out of Memory
```bash
# Increase heap size in wrapper script
java -Xmx2g -jar ~/.local/lib/pom-validator-tool.jar "$@"
```

### Getting Help

- **Documentation**: [GitHub Wiki](https://github.com/firefly-oss/pom-validator-tool/wiki)
- **Issues**: [GitHub Issues](https://github.com/firefly-oss/pom-validator-tool/issues)
- **Discussions**: [GitHub Discussions](https://github.com/firefly-oss/pom-validator-tool/discussions)
- **Email**: support@firefly-platform.com

## Uninstallation

### Using the Uninstall Script (Recommended)

```bash
# Download and run the uninstaller
curl -fsSL https://raw.githubusercontent.com/firefly-oss/pom-validator-tool/main/uninstall.sh | bash

# Or download first to review
curl -O https://raw.githubusercontent.com/firefly-oss/pom-validator-tool/main/uninstall.sh
chmod +x uninstall.sh
./uninstall.sh
```

#### Uninstall Options
```bash
./uninstall.sh --help     # Show help message
./uninstall.sh --force    # Skip confirmation prompt
./uninstall.sh --clean    # Also remove cache and temp files
```

The uninstaller will:
- Detect all installation locations
- Remove executables and JAR files
- Clean up directories
- Remove aliases from shell configs
- Verify complete removal

### Manual Uninstallation
```bash
# Remove executable and JAR
rm -f ~/.local/bin/pom-validator
rm -f ~/.local/lib/pom-validator-tool.jar

# Remove directories
rm -rf ~/.pom-validator
rm -rf ~/.config/pom-validator
rm -rf ~/.cache/pom-validator

# Remove from PATH (edit .bashrc/.zshrc)
# Remove any lines containing 'pom-validator'
```

### Package Manager
```bash
# Homebrew
brew uninstall pom-validator

# APT
sudo apt remove pom-validator

# DNF
sudo dnf remove pom-validator
```

---

**Firefly OpenCore Banking Platform** - Enterprise-grade tools for financial services development.
