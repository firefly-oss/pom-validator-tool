#!/bin/bash

# POM Validator Tool Installation Script
# This script downloads and installs the POM Validator Tool

set -e

# Configuration
TOOL_NAME="pom-validator-tool"
GITHUB_REPO="firefly-oss/pom-validator-tool"
INSTALL_DIR="$HOME/.local/bin"
TOOL_DIR="$HOME/.local/share/$TOOL_NAME"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check prerequisites
check_prerequisites() {
    print_status "Checking prerequisites..."
    
    # Check if Java 21+ is installed
    if command -v java >/dev/null 2>&1; then
        JAVA_VERSION=$(java -version 2>&1 | head -n1 | cut -d'"' -f2 | cut -d'.' -f1)
        if [ "$JAVA_VERSION" -lt 21 ]; then
            print_error "Java 21 or higher is required. Found Java $JAVA_VERSION"
            print_status "Please install Java 21+ from: https://adoptium.net/"
            exit 1
        fi
        print_success "Java $JAVA_VERSION found"
    else
        print_error "Java is not installed"
        print_status "Please install Java 21+ from: https://adoptium.net/"
        exit 1
    fi
    
    # Check if curl or wget is available
    if ! command -v curl >/dev/null 2>&1 && ! command -v wget >/dev/null 2>&1; then
        print_error "Either curl or wget is required for downloading"
        exit 1
    fi
    
    # Check if Maven is installed (optional, for building from source)
    if command -v mvn >/dev/null 2>&1; then
        MVN_VERSION=$(mvn -version 2>&1 | head -n1 | sed 's/.*Maven \([0-9.]*\).*/\1/')
        print_success "Maven $MVN_VERSION found (optional for building from source)"
    else
        print_warning "Maven not found (only needed if building from source)"
    fi
}

# Get the latest release version
get_latest_version() {
    print_status "Getting latest release version..."
    
    if command -v curl >/dev/null 2>&1; then
        LATEST_VERSION=$(curl -s "https://api.github.com/repos/$GITHUB_REPO/releases/latest" | grep '"tag_name"' | sed -E 's/.*"([^"]+)".*/\1/')
    elif command -v wget >/dev/null 2>&1; then
        LATEST_VERSION=$(wget -qO- "https://api.github.com/repos/$GITHUB_REPO/releases/latest" | grep '"tag_name"' | sed -E 's/.*"([^"]+)".*/\1/')
    fi
    
    if [ -z "$LATEST_VERSION" ]; then
        print_warning "Could not fetch latest version, using 'main' branch"
        LATEST_VERSION="main"
        USE_SOURCE=true
    else
        print_success "Latest version: $LATEST_VERSION"
        USE_SOURCE=false
    fi
}

# Create directories
create_directories() {
    print_status "Creating installation directories..."
    mkdir -p "$INSTALL_DIR"
    mkdir -p "$TOOL_DIR"
    print_success "Directories created"
}

# Download and install from source
install_from_source() {
    print_status "Installing from source..."
    
    # Create temporary directory
    TMP_DIR=$(mktemp -d)
    cd "$TMP_DIR"
    
    # Download source
    if command -v curl >/dev/null 2>&1; then
        curl -L "https://github.com/$GITHUB_REPO/archive/refs/heads/main.zip" -o source.zip
    elif command -v wget >/dev/null 2>&1; then
        wget "https://github.com/$GITHUB_REPO/archive/refs/heads/main.zip" -O source.zip
    fi
    
    # Extract and build
    unzip -q source.zip
    cd "$TOOL_NAME-main"
    
    print_status "Building with Maven..."
    if ! mvn clean package -DskipTests; then
        print_error "Failed to build from source"
        rm -rf "$TMP_DIR"
        exit 1
    fi
    
    # Copy JAR to tool directory
    cp "target/$TOOL_NAME-"*.jar "$TOOL_DIR/$TOOL_NAME.jar"
    
    # Cleanup
    rm -rf "$TMP_DIR"
    
    print_success "Built and installed from source"
}

# Download pre-built release
install_from_release() {
    print_status "Installing from release $LATEST_VERSION..."
    
    # Download JAR from release
    JAR_URL="https://github.com/$GITHUB_REPO/releases/download/$LATEST_VERSION/$TOOL_NAME-${LATEST_VERSION#v}.jar"
    
    if command -v curl >/dev/null 2>&1; then
        if ! curl -L "$JAR_URL" -o "$TOOL_DIR/$TOOL_NAME.jar"; then
            print_warning "Failed to download release, falling back to source installation"
            install_from_source
            return
        fi
    elif command -v wget >/dev/null 2>&1; then
        if ! wget "$JAR_URL" -O "$TOOL_DIR/$TOOL_NAME.jar"; then
            print_warning "Failed to download release, falling back to source installation"
            install_from_source
            return
        fi
    fi
    
    print_success "Downloaded release JAR"
}

# Create wrapper script
create_wrapper() {
    print_status "Creating wrapper script..."
    
    cat > "$INSTALL_DIR/pom-validator" << 'EOF'
#!/bin/bash

# POM Validator Tool Wrapper Script

TOOL_DIR="$HOME/.local/share/pom-validator-tool"
JAR_FILE="$TOOL_DIR/pom-validator-tool.jar"

if [ ! -f "$JAR_FILE" ]; then
    echo "Error: POM Validator Tool not found at $JAR_FILE"
    echo "Please run the installation script again."
    exit 1
fi

# Check if Java is available
if ! command -v java >/dev/null 2>&1; then
    echo "Error: Java is not installed or not in PATH"
    exit 1
fi

# Run the tool
exec java -jar "$JAR_FILE" "$@"
EOF
    
    chmod +x "$INSTALL_DIR/pom-validator"
    print_success "Wrapper script created at $INSTALL_DIR/pom-validator"
}

# Update PATH
update_path() {
    print_status "Checking PATH configuration..."
    
    # Check if install directory is in PATH
    if [[ ":$PATH:" != *":$INSTALL_DIR:"* ]]; then
        print_warning "$INSTALL_DIR is not in your PATH"
        
        # Try to add to common shell configuration files
        for SHELL_RC in "$HOME/.bashrc" "$HOME/.zshrc" "$HOME/.profile"; do
            if [ -f "$SHELL_RC" ]; then
                if ! grep -q "$INSTALL_DIR" "$SHELL_RC"; then
                    echo "export PATH=\"\$PATH:$INSTALL_DIR\"" >> "$SHELL_RC"
                    print_success "Added $INSTALL_DIR to PATH in $SHELL_RC"
                    PATH_UPDATED=true
                    break
                fi
            fi
        done
        
        if [ -z "$PATH_UPDATED" ]; then
            print_warning "Please add $INSTALL_DIR to your PATH manually:"
            echo "export PATH=\"\$PATH:$INSTALL_DIR\""
        fi
    else
        print_success "$INSTALL_DIR is already in PATH"
    fi
}

# Verify installation
verify_installation() {
    print_status "Verifying installation..."
    
    if [ -f "$TOOL_DIR/$TOOL_NAME.jar" ] && [ -f "$INSTALL_DIR/pom-validator" ]; then
        print_success "Installation completed successfully!"
        echo
        echo -e "${BLUE}Usage:${NC}"
        echo "  pom-validator /path/to/pom.xml"
        echo "  pom-validator --help"
        echo
        echo -e "${BLUE}Location:${NC}"
        echo "  Tool: $TOOL_DIR/$TOOL_NAME.jar"
        echo "  Script: $INSTALL_DIR/pom-validator"
        echo
        if [[ ":$PATH:" != *":$INSTALL_DIR:"* ]]; then
            echo -e "${YELLOW}Note: You may need to restart your shell or run:${NC}"
            echo "  source ~/.bashrc  # or ~/.zshrc"
            echo "  export PATH=\"\$PATH:$INSTALL_DIR\""
        fi
    else
        print_error "Installation verification failed"
        exit 1
    fi
}

# Main installation function
main() {
    echo -e "${BLUE}========================================${NC}"
    echo -e "${BLUE}  POM Validator Tool Installation${NC}"
    echo -e "${BLUE}========================================${NC}"
    echo
    
    check_prerequisites
    get_latest_version
    create_directories
    
    if [ "$USE_SOURCE" = true ] || [ "$1" = "--source" ]; then
        install_from_source
    else
        install_from_release
    fi
    
    create_wrapper
    update_path
    verify_installation
}

# Handle command line arguments
case "$1" in
    --help|-h)
        echo "POM Validator Tool Installation Script"
        echo
        echo "Usage: $0 [OPTIONS]"
        echo
        echo "Options:"
        echo "  --source    Install from source code (requires Maven)"
        echo "  --help      Show this help message"
        echo
        echo "This script will install the POM Validator Tool to:"
        echo "  $TOOL_DIR"
        echo "  $INSTALL_DIR/pom-validator (wrapper script)"
        exit 0
        ;;
    *)
        main "$@"
        ;;
esac
