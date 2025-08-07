# POM Validator Tool - Quick Reference

> **Firefly OpenCore Banking Platform** - Enterprise Banking Infrastructure

## Installation
```bash
curl -fsSL https://raw.githubusercontent.com/firefly-oss/pom-validator-tool/main/install.sh | bash
```

## Common Commands

### Basic Validation
```bash
pom-validator                    # Validate current directory
pom-validator pom.xml            # Validate specific file
pom-validator /path/to/project   # Validate project directory
```

### Multi-Module Projects
```bash
pom-validator -r .               # Validate all modules recursively
pom-validator -r -s .            # Summary only for multi-module
```

### Watch Mode 🔍
```bash
pom-validator -w .               # Watch current directory
pom-validator -w -r .            # Watch all modules
```

### Interactive Mode 🔧
```bash
pom-validator -i pom.xml         # Fix issues interactively
pom-validator -i .               # Interactive mode for current POM
```

### Output Formats 📊
```bash
pom-validator -o json .                      # JSON to console
pom-validator -o json -O report.json .       # JSON to file
pom-validator -o markdown -O report.md .     # Markdown report
```

### Filtering
```bash
pom-validator -S error .         # Errors only
pom-validator -S warning .       # Errors + warnings
pom-validator -p strict .        # Strict validation profile
pom-validator -e target .        # Exclude target directory
pom-validator --fail-fast .      # Stop on first error
```

### CI/CD Integration
```bash
pom-validator -q .               # Quiet mode (exit code only)
pom-validator --no-color .       # Disable colors
pom-validator -S error --fail-fast .  # CI pipeline mode
```

## Command Options

| Short | Long | Description |
|-------|------|-------------|
| `-h` | `--help` | Show help |
| `-v` | `--version` | Show version |
| `-r` | `--recursive` | Validate recursively |
| `-s` | `--summary` | Summary only |
| `-w` | `--watch` | Watch mode |
| `-i` | `--interactive` | Interactive mode |
| `-o` | `--output <fmt>` | Output format |
| `-O` | `--output-file` | Output to file |
| `-S` | `--severity <lvl>` | Severity level |
| `-p` | `--profile <name>` | Validation profile |
| `-e` | `--exclude <pat>` | Exclude pattern |
| `-I` | `--include <pat>` | Include pattern |
| `-q` | `--quiet` | Quiet mode |
| `-V` | `--verbose` | Verbose mode |
| | `--no-color` | No colors |
| | `--fail-fast` | Stop on error |

## Output Formats
- `console` (default)
- `json`
- `markdown`
- `xml` (coming soon)
- `html` (coming soon)
- `junit` (coming soon)

## Severity Levels
- `error` - Critical issues only
- `warning` - Errors and warnings
- `info` - All issues
- `all` - Everything (default)

## Validation Profiles
- `strict` - All checks enabled
- `standard` - Default checks
- `minimal` - Critical only

## Exit Codes
- `0` - Valid (no errors)
- `1` - Validation errors found
- `2` - Tool error

## Banking Use Cases

### Compliance Check
```bash
pom-validator -p strict -o json -O compliance.json .
```

### Release Validation
```bash
pom-validator -S error . | grep -v SNAPSHOT
```

### Team Reports
```bash
for team in payments accounts loans; do
  pom-validator -r -o markdown -O ${team}-report.md ${team}/
done
```

### Pre-commit Hook
```bash
#!/bin/bash
# .git/hooks/pre-commit
pom-validator -q . || exit 1
```

## Tips & Tricks

### Check specific modules
```bash
pom-validator account-service/pom.xml payment-service/pom.xml
```

### Generate documentation
```bash
pom-validator -o markdown -O docs/POM_STATUS.md -r .
```

### Monitor during development
```bash
pom-validator -w . &  # Run in background
```

### Quick CI check
```bash
pom-validator -S error --fail-fast --no-color -q .
echo $?  # Check exit code
```

### Fix issues one by one
```bash
pom-validator -i pom.xml
```

## Support

- 📖 [Full Documentation](https://github.com/firefly-oss/pom-validator-tool)
- 🐛 [Report Issues](https://github.com/firefly-oss/pom-validator-tool/issues)
- 💬 [Discussions](https://github.com/firefly-oss/pom-validator-tool/discussions)
- 📧 [Security](mailto:security@firefly-platform.com)

---

**Firefly OpenCore Banking Platform** - Building trust in financial technology through open source.
