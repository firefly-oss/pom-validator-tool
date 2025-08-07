package com.catalis.tools.pomvalidator.validator;

import com.catalis.tools.pomvalidator.model.ValidationResult;
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
            result.error("Model version must be 4.0.0, found: " + model.getModelVersion());
        }
        
        // Check required GAV coordinates
        if (isBlank(model.getGroupId())) {
            result.error("GroupId is missing");
        }
        
        if (isBlank(model.getArtifactId())) {
            result.error("ArtifactId is missing");
        }
        
        if (isBlank(model.getVersion())) {
            result.error("Version is missing");
        }
        
        // Check packaging (default is jar if not specified)
        String packaging = model.getPackaging();
        if (packaging != null && !isValidPackaging(packaging)) {
            result.warning("Unknown packaging type: " + packaging);
        }
        
        // Check for required elements in parent POM
        if ("pom".equals(packaging)) {
            if (model.getModules() == null || model.getModules().isEmpty()) {
                if (model.getDependencyManagement() == null && 
                    (model.getBuild() == null || model.getBuild().getPluginManagement() == null)) {
                    result.warning("POM packaging without modules should typically have dependency or plugin management");
                }
            }
        }
        
        // Info about the project
        result.info("GAV: " + model.getGroupId() + ":" + model.getArtifactId() + ":" + model.getVersion());
        result.info("Packaging: " + (packaging != null ? packaging : "jar (default)"));
        
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
