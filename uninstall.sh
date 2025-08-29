#!/bin/bash

# POM Validator Tool Uninstaller
# This script removes the POM Validator Tool from your system

set -e

# Check if we're running from a pipe (curl | bash)
if [ ! -t 0 ]; then
    echo "⚠️  Running from a pipe detected. For interactive mode with sudo, please download and run directly:"
    echo ""
    echo "  curl -O https://raw.githubusercontent.com/firefly-oss/pom-validator-tool/main/uninstall.sh"
    echo "  chmod +x uninstall.sh"
    echo "  ./uninstall.sh"
    echo ""
    echo "Or run with --force flag to skip confirmation:"
    echo "  curl -fsSL ... | bash -s -- --force"
    echo ""
    # If no TTY and no --force flag, exit
    if [[ ! " $* " =~ " --force " ]] && [[ ! " $* " =~ " -f " ]]; then
        exit 1
    fi
fi

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_color() {
    echo -e "${1}${2}${NC}"
}

# Function to print header
print_header() {
    echo ""
    print_color "$BLUE" "═══════════════════════════════════════════════════════"
    print_color "$BLUE" "         POM Validator Tool Uninstaller"
    print_color "$BLUE" "═══════════════════════════════════════════════════════"
    echo ""
}

# Function to check if running with appropriate permissions
check_permissions() {
    if [ "$EUID" -ne 0 ] && [ ! -w "/usr/local/bin" ]; then
        print_color "$YELLOW" "⚠️  Warning: This script may need sudo permissions to remove files from /usr/local/bin"
        print_color "$YELLOW" "   If prompted, please enter your password."
        echo ""
    fi
}

# Function to remove file with sudo if needed
remove_file() {
    local file="$1"
    if [ -f "$file" ]; then
        if [ -w "$file" ] || [ -w "$(dirname "$file")" ]; then
            rm -f "$file" 2>/dev/null || {
                echo "Need sudo to remove $file"
                if command -v sudo &> /dev/null; then
                    sudo rm -f "$file"
                else
                    echo "❌ Cannot remove $file without write permissions"
                    return 1
                fi
            }
        else
            echo "Need sudo to remove $file"
            if command -v sudo &> /dev/null; then
                sudo rm -f "$file"
            else
                echo "❌ Cannot remove $file without write permissions"
                return 1
            fi
        fi
        return 0
    fi
    return 1
}

# Function to remove directory with sudo if needed
remove_directory() {
    local dir="$1"
    if [ -d "$dir" ]; then
        if [ -w "$dir" ] || [ -w "$(dirname "$dir")" ]; then
            rm -rf "$dir" 2>/dev/null || {
                echo "Need sudo to remove $dir"
                if command -v sudo &> /dev/null; then
                    sudo rm -rf "$dir"
                else
                    echo "❌ Cannot remove $dir without write permissions"
                    return 1
                fi
            }
        else
            echo "Need sudo to remove $dir"
            if command -v sudo &> /dev/null; then
                sudo rm -rf "$dir"
            else
                echo "❌ Cannot remove $dir without write permissions"
                return 1
            fi
        fi
        return 0
    fi
    return 1
}

# Function to detect installation locations
detect_installations() {
    local found=0
    
    print_color "$BLUE" "🔍 Detecting POM Validator Tool installations..."
    echo ""
    
    # Check common installation locations
    local locations=(
        "/usr/local/bin/pom-validator"
        "/usr/bin/pom-validator"
        "$HOME/.local/bin/pom-validator"
        "$HOME/bin/pom-validator"
        "/opt/pom-validator"
        "$HOME/.pom-validator"
    )
    
    for location in "${locations[@]}"; do
        if [ -f "$location" ] || [ -d "$location" ]; then
            print_color "$GREEN" "   ✓ Found: $location"
            found=1
        fi
    done
    
    # Check if installed via package manager
    if command -v pom-validator &> /dev/null; then
        local cmd_path=$(which pom-validator)
        print_color "$GREEN" "   ✓ Found command: $cmd_path"
        found=1
    fi
    
    # Check for JAR file in common locations
    local jar_locations=(
        "/usr/local/lib/pom-validator.jar"
        "/usr/share/java/pom-validator.jar"
        "$HOME/.local/share/pom-validator/pom-validator.jar"
    )
    
    for jar in "${jar_locations[@]}"; do
        if [ -f "$jar" ]; then
            print_color "$GREEN" "   ✓ Found JAR: $jar"
            found=1
        fi
    done
    
    if [ $found -eq 0 ]; then
        print_color "$YELLOW" "   ⚠️  No installations found in common locations"
        echo ""
        return 1
    fi
    
    echo ""
    return 0
}

# Function to uninstall
uninstall() {
    local removed=0
    
    print_color "$BLUE" "🗑️  Removing POM Validator Tool..."
    echo ""
    
    # Remove executable/script
    local executables=(
        "/usr/local/bin/pom-validator"
        "/usr/bin/pom-validator"
        "$HOME/.local/bin/pom-validator"
        "$HOME/bin/pom-validator"
    )
    
    for exe in "${executables[@]}"; do
        if remove_file "$exe"; then
            print_color "$GREEN" "   ✓ Removed: $exe"
            removed=1
        fi
    done
    
    # Remove JAR files
    local jars=(
        "/usr/local/lib/pom-validator.jar"
        "/usr/local/lib/pom-validator-tool.jar"
        "/usr/share/java/pom-validator.jar"
        "/usr/share/java/pom-validator-tool.jar"
        "$HOME/.local/share/pom-validator/pom-validator.jar"
        "$HOME/.local/share/pom-validator/pom-validator-tool.jar"
    )
    
    for jar in "${jars[@]}"; do
        if remove_file "$jar"; then
            print_color "$GREEN" "   ✓ Removed: $jar"
            removed=1
        fi
    done
    
    # Remove directories
    local directories=(
        "/opt/pom-validator"
        "$HOME/.pom-validator"
        "$HOME/.local/share/pom-validator"
        "$HOME/.config/pom-validator"
    )
    
    for dir in "${directories[@]}"; do
        if remove_directory "$dir"; then
            print_color "$GREEN" "   ✓ Removed: $dir"
            removed=1
        fi
    done
    
    # Remove any aliases from shell configs
    local shell_configs=(
        "$HOME/.bashrc"
        "$HOME/.bash_profile"
        "$HOME/.zshrc"
        "$HOME/.profile"
    )
    
    for config in "${shell_configs[@]}"; do
        if [ -f "$config" ]; then
            # Create backup before modifying
            cp "$config" "$config.backup.$(date +%Y%m%d%H%M%S)"
            
            # Remove alias lines
            if grep -q "alias.*pom-validator" "$config" 2>/dev/null; then
                sed -i.bak '/alias.*pom-validator/d' "$config" 2>/dev/null || \
                sed -i '' '/alias.*pom-validator/d' "$config" 2>/dev/null
                print_color "$GREEN" "   ✓ Removed alias from: $config"
                removed=1
            fi
            
            # Remove PATH additions
            if grep -q "pom-validator" "$config" 2>/dev/null; then
                sed -i.bak '/pom-validator/d' "$config" 2>/dev/null || \
                sed -i '' '/pom-validator/d' "$config" 2>/dev/null
                print_color "$GREEN" "   ✓ Cleaned PATH in: $config"
                removed=1
            fi
        fi
    done
    
    echo ""
    
    if [ $removed -eq 0 ]; then
        print_color "$YELLOW" "⚠️  No files were removed. The tool may not be installed or is installed in a non-standard location."
        return 1
    fi
    
    return 0
}

# Function to verify uninstallation
verify_uninstall() {
    print_color "$BLUE" "🔍 Verifying uninstallation..."
    echo ""
    
    local issues=0
    
    # Check if command still exists
    if command -v pom-validator &> /dev/null; then
        print_color "$RED" "   ✗ Command 'pom-validator' still exists at: $(which pom-validator)"
        issues=1
    else
        print_color "$GREEN" "   ✓ Command 'pom-validator' successfully removed"
    fi
    
    # Check common locations
    local check_locations=(
        "/usr/local/bin/pom-validator"
        "/opt/pom-validator"
        "$HOME/.pom-validator"
    )
    
    for location in "${check_locations[@]}"; do
        if [ -e "$location" ]; then
            print_color "$RED" "   ✗ Still exists: $location"
            issues=1
        fi
    done
    
    echo ""
    
    if [ $issues -eq 0 ]; then
        print_color "$GREEN" "✅ POM Validator Tool has been successfully uninstalled!"
        return 0
    else
        print_color "$YELLOW" "⚠️  Some components may still be present. Please check the locations above."
        return 1
    fi
}

# Function to clean cache and temporary files
clean_cache() {
    print_color "$BLUE" "🧹 Cleaning cache and temporary files..."
    echo ""
    
    local cache_dirs=(
        "$HOME/.cache/pom-validator"
        "/tmp/pom-validator*"
        "$HOME/.m2/repository/com/firefly/tools/pom-validator-tool"
    )
    
    for cache in "${cache_dirs[@]}"; do
        if [ -e "$cache" ] || ls $cache 2>/dev/null; then
            rm -rf $cache 2>/dev/null
            print_color "$GREEN" "   ✓ Cleaned: $cache"
        fi
    done
    
    echo ""
}

# Function to show usage
show_usage() {
    echo "Usage: $0 [OPTIONS]"
    echo ""
    echo "Options:"
    echo "  -h, --help       Show this help message"
    echo "  -f, --force      Force uninstall without confirmation"
    echo "  -c, --clean      Also remove cache and temporary files"
    echo "  -v, --verbose    Show verbose output"
    echo ""
    echo "Examples:"
    echo "  $0               # Interactive uninstall"
    echo "  $0 --force       # Uninstall without confirmation"
    echo "  $0 --clean       # Uninstall and clean cache"
}

# Main function
main() {
    local force=0
    local clean=0
    local verbose=0
    
    # Parse arguments
    while [[ $# -gt 0 ]]; do
        case $1 in
            -h|--help)
                show_usage
                exit 0
                ;;
            -f|--force)
                force=1
                shift
                ;;
            -c|--clean)
                clean=1
                shift
                ;;
            -v|--verbose)
                verbose=1
                shift
                ;;
            *)
                print_color "$RED" "Unknown option: $1"
                show_usage
                exit 1
                ;;
        esac
    done
    
    print_header
    check_permissions
    
    # Detect installations
    detect_installations
    
    # Ask for confirmation if not forced
    if [ $force -eq 0 ]; then
        print_color "$YELLOW" "⚠️  This will remove POM Validator Tool from your system."
        echo ""
        
        # Check if we can read from terminal
        if [ -t 0 ]; then
            read -p "Are you sure you want to continue? (y/N): " -n 1 -r
            echo ""
            echo ""
            
            if [[ ! $REPLY =~ ^[Yy]$ ]]; then
                print_color "$BLUE" "Uninstallation cancelled."
                exit 0
            fi
        else
            print_color "$YELLOW" "Running in non-interactive mode. Use --force to skip confirmation."
            exit 1
        fi
    fi
    
    # Perform uninstallation
    if uninstall; then
        # Clean cache if requested
        if [ $clean -eq 1 ]; then
            clean_cache
        fi
        
        # Verify uninstallation
        verify_uninstall
        
        print_color "$BLUE" "═══════════════════════════════════════════════════════"
        print_color "$GREEN" "Thank you for using POM Validator Tool!"
        print_color "$BLUE" "═══════════════════════════════════════════════════════"
        echo ""
        
        # Remind about shell reload
        print_color "$YELLOW" "💡 Note: You may need to reload your shell or open a new terminal"
        print_color "$YELLOW" "   for the changes to take full effect."
        echo ""
        print_color "$YELLOW" "   Run: source ~/.bashrc  (or ~/.zshrc for zsh)"
        echo ""
    else
        print_color "$RED" "❌ Uninstallation encountered issues. Please check the output above."
        exit 1
    fi
}

# Run main function
main "$@"
