package com.catalis.tools.pomvalidator.validator;

import com.catalis.tools.pomvalidator.model.ValidationResult;
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
        ValidationResult.ValidationResultBuilder result = ValidationResult.builder();
        
        Properties properties = model.getProperties();
        
        if (properties == null || properties.isEmpty()) {
            result.warning("No properties defined - consider adding standard Maven properties");
            return result.build();
        }
        
        // Check for recommended properties
        for (String recommendedProp : RECOMMENDED_PROPERTIES) {
            if (!properties.containsKey(recommendedProp)) {
                result.warning("Missing recommended property: " + recommendedProp);
            }
        }
        
        // Check encoding properties
        String sourceEncoding = properties.getProperty("project.build.sourceEncoding");
        if (sourceEncoding != null && !"UTF-8".equals(sourceEncoding)) {
            result.warning("Consider using UTF-8 encoding: " + sourceEncoding);
        }
        
        String reportingEncoding = properties.getProperty("project.reporting.outputEncoding");
        if (reportingEncoding != null && !"UTF-8".equals(reportingEncoding)) {
            result.warning("Consider using UTF-8 for reporting encoding: " + reportingEncoding);
        }
        
        // Check Java version consistency
        String compilerSource = properties.getProperty("maven.compiler.source");
        String compilerTarget = properties.getProperty("maven.compiler.target");
        String javaVersion = properties.getProperty("java.version");
        
        if (compilerSource != null && compilerTarget != null) {
            if (!compilerSource.equals(compilerTarget)) {
                result.warning("Compiler source and target versions differ: " + 
                             compilerSource + " vs " + compilerTarget);
            }
        }
        
        if (javaVersion != null) {
            if (compilerSource != null && !compilerSource.equals(javaVersion)) {
                result.warning("Java version and compiler source mismatch: " + 
                             javaVersion + " vs " + compilerSource);
            }
            
            // Check for modern Java version
            checkJavaVersion(javaVersion, result);
        }
        
        // Check for unused properties (properties that don't appear to be referenced)
        checkUnusedProperties(properties, result);
        
        result.info("Properties defined: " + properties.size());
        
        return result.build();
    }
    
    private void checkJavaVersion(String javaVersion, ValidationResult.ValidationResultBuilder result) {
        try {
            int majorVersion = Integer.parseInt(javaVersion);
            if (majorVersion < 11) {
                result.warning("Java version " + javaVersion + " is no longer supported. Consider upgrading to Java 11+");
            } else if (majorVersion >= 11) {
                result.info("Using supported Java version: " + javaVersion);
            }
        } catch (NumberFormatException e) {
            // Try to parse versions like "1.8"
            if (javaVersion.startsWith("1.")) {
                result.warning("Java version " + javaVersion + " uses old versioning scheme and is likely outdated");
            }
        }
    }
    
    private void checkUnusedProperties(Properties properties, ValidationResult.ValidationResultBuilder result) {
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
            result.info("Found " + potentiallyUnused + " potentially unused custom properties");
        }
    }
    
    private boolean isStandardProperty(String propertyName) {
        return propertyName.startsWith("project.") ||
               propertyName.startsWith("maven.") ||
               propertyName.equals("java.version") ||
               propertyName.endsWith(".version");
    }
}
