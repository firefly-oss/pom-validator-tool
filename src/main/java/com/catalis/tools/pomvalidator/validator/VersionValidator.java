package com.catalis.tools.pomvalidator.validator;

import com.catalis.tools.pomvalidator.model.ValidationResult;
import org.apache.maven.model.Model;
import org.apache.maven.model.Parent;

import java.nio.file.Path;
import java.util.regex.Pattern;

/**
 * Validates version-related issues like version format, SNAPSHOT consistency, etc.
 */
public class VersionValidator implements PomValidator {
    
    // Common version patterns
    private static final Pattern SEMANTIC_VERSION_PATTERN = 
        Pattern.compile("^\\d+\\.\\d+\\.\\d+(-[a-zA-Z0-9.-]+)?$");
    private static final Pattern MAVEN_VERSION_PATTERN = 
        Pattern.compile("^\\d+(\\.\\d+)*(-[a-zA-Z0-9.-]+)?(-SNAPSHOT)?$");
    
    @Override
    public ValidationResult validate(Model model, Path pomPath) {
        ValidationResult.ValidationResultBuilder result = ValidationResult.builder();
        
        // Validate project version
        String projectVersion = model.getVersion();
        if (projectVersion != null) {
            validateVersion(projectVersion, "project", result);
        }
        
        // Validate parent version if present
        Parent parent = model.getParent();
        if (parent != null && parent.getVersion() != null) {
            validateVersion(parent.getVersion(), "parent", result);
            
            // Check version consistency between parent and project
            checkParentVersionConsistency(parent, model, result);
        }
        
        // Check for version consistency in the project
        checkVersionConsistency(model, result);
        
        return result.build();
    }
    
    private void validateVersion(String version, String type, 
                               ValidationResult.ValidationResultBuilder result) {
        if (isBlank(version)) {
            result.error("Empty " + type + " version");
            return;
        }
        
        // Check for basic version format
        if (!MAVEN_VERSION_PATTERN.matcher(version).matches()) {
            result.warning("Non-standard " + type + " version format: " + version);
        }
        
        // Check for LATEST or RELEASE (deprecated)
        if ("LATEST".equals(version) || "RELEASE".equals(version)) {
            result.error("Deprecated version keyword in " + type + ": " + version);
        }
        
        // Check for version ranges
        if (version.contains("[") || version.contains("(") || version.contains(",")) {
            result.warning("Version range in " + type + ": " + version);
        }
        
        // Check for SNAPSHOT versions
        if (version.endsWith("-SNAPSHOT")) {
            result.info("SNAPSHOT " + type + " version: " + version);
        }
        
        // Check for semantic versioning compliance
        if (SEMANTIC_VERSION_PATTERN.matcher(version.replace("-SNAPSHOT", "")).matches()) {
            result.info("Semantic versioning compliant " + type + " version: " + version);
        }
        
        // Warn about potential version issues
        checkVersionBestPractices(version, type, result);
    }
    
    private void checkVersionBestPractices(String version, String type, 
                                         ValidationResult.ValidationResultBuilder result) {
        // Check for very long version strings
        if (version.length() > 50) {
            result.warning("Very long " + type + " version string: " + version);
        }
        
        // Check for versions starting with 'v'
        if (version.toLowerCase().startsWith("v")) {
            result.warning("Version starts with 'v' - consider removing: " + version);
        }
        
        // Check for multiple hyphens in version (could indicate issues)
        long hyphenCount = version.chars().filter(ch -> ch == '-').count();
        if (hyphenCount > 2) {
            result.warning("Version contains many hyphens, verify format: " + version);
        }
        
        // Check for common version anti-patterns
        if (version.toLowerCase().contains("final")) {
            result.warning("Version contains 'FINAL' - this is usually implicit: " + version);
        }
    }
    
    private void checkParentVersionConsistency(Parent parent, Model model, 
                                             ValidationResult.ValidationResultBuilder result) {
        String parentVersion = parent.getVersion();
        String projectVersion = model.getVersion();
        
        // If both are SNAPSHOT, they should typically match
        if (parentVersion != null && projectVersion != null) {
            boolean parentIsSnapshot = parentVersion.endsWith("-SNAPSHOT");
            boolean projectIsSnapshot = projectVersion.endsWith("-SNAPSHOT");
            
            if (parentIsSnapshot != projectIsSnapshot) {
                result.warning("Parent and project SNAPSHOT status mismatch: parent=" + 
                             parentVersion + ", project=" + projectVersion);
            }
            
            // If parent is newer release and project is still snapshot, might be issue
            if (!parentIsSnapshot && projectIsSnapshot) {
                result.info("Parent is release version while project is SNAPSHOT - " +
                           "consider if project should also be released");
            }
        }
    }
    
    private void checkVersionConsistency(Model model, ValidationResult.ValidationResultBuilder result) {
        String version = model.getVersion();
        if (version == null) {
            return;
        }
        
        // Check artifact naming consistency with version
        String artifactId = model.getArtifactId();
        if (artifactId != null) {
            // Check if version type matches artifact naming convention
            if (version.endsWith("-SNAPSHOT") && artifactId.toLowerCase().contains("release")) {
                result.warning("Artifact name suggests release but version is SNAPSHOT: " + 
                             artifactId + " vs " + version);
            }
            
            if (!version.endsWith("-SNAPSHOT") && artifactId.toLowerCase().contains("snapshot")) {
                result.warning("Artifact name suggests SNAPSHOT but version is not: " + 
                             artifactId + " vs " + version);
            }
        }
        
        // Check if this looks like a development version
        if (version.contains("dev") || version.contains("alpha") || version.contains("beta")) {
            result.info("Development/pre-release version detected: " + version);
        }
        
        // Check for numeric-only versions (might indicate issues)
        if (version.matches("^\\d+$")) {
            result.warning("Single number version - consider using semantic versioning: " + version);
        }
    }
    
    private boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }
}
