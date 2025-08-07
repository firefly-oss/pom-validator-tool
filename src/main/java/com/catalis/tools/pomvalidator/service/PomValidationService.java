package com.catalis.tools.pomvalidator.service;

import com.catalis.tools.pomvalidator.model.ValidationResult;
import com.catalis.tools.pomvalidator.validator.*;
import com.catalis.tools.pomvalidator.util.PomParser;
import org.apache.maven.model.Model;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

/**
 * Main service for validating POM files.
 * Orchestrates various validators to perform comprehensive POM validation.
 */
public class PomValidationService {
    
    private final List<PomValidator> validators;
    private final PomParser pomParser;
    
    public PomValidationService() {
        this.pomParser = new PomParser();
        this.validators = Arrays.asList(
            new BasicStructureValidator(),
            new DependencyValidator(),
            new PropertyValidator(),
            new PluginValidator(),
            new VersionValidator(),
            new BestPracticesValidator()
        );
    }
    
    /**
     * Validates a POM file and returns comprehensive validation results.
     */
    public ValidationResult validatePom(Path pomPath) throws Exception {
        ValidationResult.ValidationResultBuilder resultBuilder = ValidationResult.builder();
        
        // Check if file exists
        if (!Files.exists(pomPath)) {
            return ValidationResult.builder()
                .error("POM file does not exist: " + pomPath)
                .build();
        }
        
        // Check if it's actually a file
        if (!Files.isRegularFile(pomPath)) {
            return ValidationResult.builder()
                .error("Path is not a regular file: " + pomPath)
                .build();
        }
        
        try {
            // Parse the POM file
            Model model = pomParser.parsePom(pomPath);
            
            if (model == null) {
                return ValidationResult.builder()
                    .error("Failed to parse POM file - invalid XML or structure")
                    .build();
            }
            
            // Run all validators
            for (PomValidator validator : validators) {
                ValidationResult validatorResult = validator.validate(model, pomPath);
                
                // Merge results
                validatorResult.getErrors().forEach(resultBuilder::error);
                validatorResult.getWarnings().forEach(resultBuilder::warning);
                validatorResult.getInfos().forEach(resultBuilder::info);
            }
            
        } catch (Exception e) {
            resultBuilder.error("Failed to validate POM: " + e.getMessage());
        }
        
        return resultBuilder.build();
    }
}
