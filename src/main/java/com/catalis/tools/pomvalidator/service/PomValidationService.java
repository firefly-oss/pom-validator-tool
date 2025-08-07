package com.catalis.tools.pomvalidator.service;

import com.catalis.tools.pomvalidator.model.ValidationResult;
import com.catalis.tools.pomvalidator.model.ValidationIssue;
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
            new MultiModuleValidator()
            // Temporarily disabled validators with compilation issues:
            // new VersionValidator(),
            // new BestPracticesValidator()
        );
    }
    
    /**
     * Validates a POM file and returns comprehensive validation results.
     */
    public ValidationResult validatePom(Path pomPath) throws Exception {
        ValidationResult result = new ValidationResult();
        
        // Check if file exists
        if (!Files.exists(pomPath)) {
            result.addError(ValidationIssue.of("POM file does not exist: " + pomPath, 
                       "Ensure the file path is correct and the file exists"));
            return result;
        }
        
        // Check if it's actually a file
        if (!Files.isRegularFile(pomPath)) {
            result.addError(ValidationIssue.of("Path is not a regular file: " + pomPath,
                       "Provide a path to a pom.xml file, not a directory"));
            return result;
        }
        
        try {
            // Parse the POM file
            Model model = pomParser.parsePom(pomPath);
            
            if (model == null) {
                result.addError(ValidationIssue.of("Failed to parse POM file - invalid XML or structure",
                           "Check that the XML is well-formed and follows Maven POM schema"));
                return result;
            }
            
            // Run all validators
            for (PomValidator validator : validators) {
                ValidationResult validatorResult = validator.validate(model, pomPath);
                
                // Merge results
                result.addErrors(validatorResult.getErrors());
                result.addWarnings(validatorResult.getWarnings());
                result.addInfos(validatorResult.getInfos());
            }
            
        } catch (Exception e) {
            result.addError(ValidationIssue.of("Failed to validate POM: " + e.getMessage(),
                   "Check the POM syntax and ensure all required elements are present"));
        }
        
        return result;
    }
}
