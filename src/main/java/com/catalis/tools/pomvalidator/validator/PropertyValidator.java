package com.catalis.tools.pomvalidator.validator;

import com.catalis.tools.pomvalidator.model.ValidationResult;
import com.catalis.tools.pomvalidator.model.ValidationIssue;
import org.apache.maven.model.Model;

import java.nio.file.Path;
import java.util.Properties;
import java.util.Set;

/**
 * Validates property-related issues like missing standard properties, encoding, etc.
 */
public class PropertyValidator implements PomValidator {
    
    private static final Set<String> RECOMMENDED_PROPERTIES = Set.of(
        "project.build.sourceEncoding",
        "project.reporting.outputEncoding",
        "maven.compiler.source",
        "maven.compiler.target"
    );
    
    @Override
    public ValidationResult validate(Model model, Path pomPath) {
        ValidationResult.Builder result = ValidationResult.builder();
        
        Properties properties = model.getProperties();
        
        if (properties == null || properties.isEmpty()) {
            result.warning(ValidationIssue.of(
                "No properties defined - consider adding standard Maven properties",
                "Add <properties> section with project.build.sourceEncoding=UTF-8, maven.compiler.source=21, etc."
            ));
            return result.build();
        }
        
        // Check for recommended properties
        for (String recommendedProp : RECOMMENDED_PROPERTIES) {
            if (!properties.containsKey(recommendedProp)) {
                String suggestion = getPropertySuggestion(recommendedProp);
                result.warning(ValidationIssue.of(
                    "Missing recommended property: " + recommendedProp,
                    suggestion
                ));
            }
        }
        
        // Check encoding properties
        String sourceEncoding = properties.getProperty("project.build.sourceEncoding");
        if (sourceEncoding != null && !"UTF-8".equals(sourceEncoding)) {
            result.warning(ValidationIssue.of(
                "Consider using UTF-8 encoding: " + sourceEncoding,
                "Set <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>"
            ));
        }
        
        String reportingEncoding = properties.getProperty("project.reporting.outputEncoding");
        if (reportingEncoding != null && !"UTF-8".equals(reportingEncoding)) {
            result.warning(ValidationIssue.of(
                "Consider using UTF-8 for reporting encoding: " + reportingEncoding,
                "Set <project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>"
            ));
        }
        
        // Check Java version consistency
        String compilerSource = properties.getProperty("maven.compiler.source");
        String compilerTarget = properties.getProperty("maven.compiler.target");
        String javaVersion = properties.getProperty("java.version");
        
        if (compilerSource != null && compilerTarget != null) {
            if (!compilerSource.equals(compilerTarget)) {
                result.warning(ValidationIssue.of(
                    "Compiler source and target versions differ: " + compilerSource + " vs " + compilerTarget,
                    "Set both maven.compiler.source and maven.compiler.target to the same Java version"
                ));
            }
        }
        
        if (javaVersion != null) {
            // Only flag mismatch if compilerSource is not a property reference
            if (compilerSource != null && !compilerSource.equals(javaVersion) && !compilerSource.contains("${java.version}")) {
                result.warning(ValidationIssue.of(
                    "Java version and compiler source mismatch: " + javaVersion + " vs " + compilerSource,
                    "Set <maven.compiler.source>${java.version}</maven.compiler.source> to use the java.version property"
                ));
            }
            
            // Check for modern Java version
            checkJavaVersion(javaVersion, result);
        }
        
        // Check for unused properties (properties that don't appear to be referenced)
        checkUnusedProperties(properties, result);
        
        result.info(ValidationIssue.of("Properties defined: " + properties.size()));
        
        return result.build();
    }
    
    private void checkJavaVersion(String javaVersion, ValidationResult.Builder result) {
        try {
            int majorVersion = Integer.parseInt(javaVersion);
            if (majorVersion < 11) {
                result.warning(ValidationIssue.of(
                    "Java version " + javaVersion + " is no longer supported. Consider upgrading to Java 11+",
                    "Update <java.version>21</java.version> for latest LTS support"
                ));
            } else if (majorVersion >= 11) {
                result.info(ValidationIssue.of("Using supported Java version: " + javaVersion));
            }
        } catch (NumberFormatException e) {
            // Try to parse versions like "1.8"
            if (javaVersion.startsWith("1.")) {
                result.warning(ValidationIssue.of(
                    "Java version " + javaVersion + " uses old versioning scheme and is likely outdated",
                    "Update to modern Java versioning: <java.version>21</java.version>"
                ));
            }
        }
    }
    
    private void checkUnusedProperties(Properties properties, ValidationResult.Builder result) {
        // This is a simplified check - in a real implementation, you might parse
        // the entire POM content to check for property references
        int potentiallyUnused = 0;
        
        for (String propertyName : properties.stringPropertyNames()) {
            // Skip standard Maven properties
            if (isStandardProperty(propertyName)) {
                continue;
            }
            
            String value = properties.getProperty(propertyName);
            if (value != null && !value.contains("${")) {
                // Property doesn't seem to reference other properties
                // and isn't a standard property, might be unused
                potentiallyUnused++;
            }
        }
        
        if (potentiallyUnused > 0) {
            result.info(ValidationIssue.of(
                "Found " + potentiallyUnused + " potentially unused custom properties",
                "Review custom properties and remove any that are not referenced in the POM"
            ));
        }
    }
    
    private boolean isStandardProperty(String propertyName) {
        return propertyName.startsWith("project.") ||
               propertyName.startsWith("maven.") ||
               propertyName.equals("java.version") ||
               propertyName.endsWith(".version");
    }
    
    private String getPropertySuggestion(String propertyName) {
        return switch (propertyName) {
            case "project.build.sourceEncoding" -> 
                "Add <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>";
            case "project.reporting.outputEncoding" -> 
                "Add <project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>";
            case "maven.compiler.source" -> 
                "Add <maven.compiler.source>21</maven.compiler.source> (or your target Java version)";
            case "maven.compiler.target" -> 
                "Add <maven.compiler.target>21</maven.compiler.target> (or your target Java version)";
            default -> "Add <" + propertyName + ">appropriate-value</" + propertyName + ">";
        };
    }
}
