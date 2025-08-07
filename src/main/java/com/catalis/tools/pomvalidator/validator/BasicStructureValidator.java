package com.catalis.tools.pomvalidator.validator;

import com.catalis.tools.pomvalidator.model.ValidationResult;
import com.catalis.tools.pomvalidator.model.ValidationIssue;
import org.apache.maven.model.Model;

import java.nio.file.Path;

/**
 * Validates basic POM structure requirements like required elements and model version.
 */
public class BasicStructureValidator implements PomValidator {
    
    @Override
    public ValidationResult validate(Model model, Path pomPath) {
        ValidationResult.ValidationResultBuilder result = ValidationResult.builder();
        
        // Check model version
        if (!"4.0.0".equals(model.getModelVersion())) {
            result.error(ValidationIssue.of(
                "Model version must be 4.0.0, found: " + model.getModelVersion(),
                "Update <modelVersion>4.0.0</modelVersion> in your POM"
            ));
        }
        
        // Check required GAV coordinates
        if (isBlank(model.getGroupId())) {
            result.error(ValidationIssue.of(
                "GroupId is missing",
                "Add <groupId>com.example</groupId> element to identify your organization/project"
            ));
        }
        
        if (isBlank(model.getArtifactId())) {
            result.error(ValidationIssue.of(
                "ArtifactId is missing",
                "Add <artifactId>your-project-name</artifactId> element to identify your project"
            ));
        }
        
        if (isBlank(model.getVersion())) {
            result.error(ValidationIssue.of(
                "Version is missing",
                "Add <version>1.0.0-SNAPSHOT</version> or inherit from parent POM"
            ));
        }
        
        // Check packaging (default is jar if not specified)
        String packaging = model.getPackaging();
        if (packaging != null && !isValidPackaging(packaging)) {
            result.warning(ValidationIssue.of(
                "Unknown packaging type: " + packaging,
                "Use standard packaging types: pom, jar, war, ear, maven-plugin, rar, or bundle"
            ));
        }
        
        // Check for required elements in parent POM
        if ("pom".equals(packaging)) {
            if (model.getModules() == null || model.getModules().isEmpty()) {
                if (model.getDependencyManagement() == null && 
                    (model.getBuild() == null || model.getBuild().getPluginManagement() == null)) {
                    result.warning(ValidationIssue.of(
                        "POM packaging without modules should typically have dependency or plugin management",
                        "Add <dependencyManagement> or <pluginManagement> sections, or define <modules>"
                    ));
                }
            }
        }
        
        // Info about the project
        result.info(ValidationIssue.of("GAV: " + model.getGroupId() + ":" + model.getArtifactId() + ":" + model.getVersion()));
        result.info(ValidationIssue.of("Packaging: " + (packaging != null ? packaging : "jar (default)")));
        
        return result.build();
    }
    
    private boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }
    
    private boolean isValidPackaging(String packaging) {
        return switch (packaging) {
            case "pom", "jar", "war", "ear", "maven-plugin", "rar", "bundle" -> true;
            default -> false;
        };
    }
}
