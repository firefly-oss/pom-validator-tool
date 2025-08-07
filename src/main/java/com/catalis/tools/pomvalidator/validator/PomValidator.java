package com.catalis.tools.pomvalidator.validator;

import com.catalis.tools.pomvalidator.model.ValidationResult;
import org.apache.maven.model.Model;

import java.nio.file.Path;

/**
 * Interface for all POM validators.
 * Each validator is responsible for a specific aspect of POM validation.
 */
public interface PomValidator {
    
    /**
     * Validates a given Maven Model.
     *
     * @param model The Maven model to validate.
     * @param pomPath The path to the POM file (for context).
     * @return A ValidationResult containing any findings.
     */
    ValidationResult validate(Model model, Path pomPath);
}
