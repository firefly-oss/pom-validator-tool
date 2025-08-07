package com.catalis.tools.pomvalidator.model;

import lombok.Data;
import lombok.Builder;
import lombok.Singular;

import java.util.List;

/**
 * Represents the result of POM validation containing errors, warnings, informational messages, and suggestions.
 */
@Data
@Builder
public class ValidationResult {
    
    @Singular
    private final List<ValidationIssue> errors;
    
    @Singular
    private final List<ValidationIssue> warnings;
    
    @Singular
    private final List<ValidationIssue> infos;
    
    /**
     * Returns true if the validation passed (no errors).
     * Warnings and info messages don't affect validity.
     */
    public boolean isValid() {
        return errors.isEmpty();
    }
    
    /**
     * Returns the total number of issues found (errors + warnings).
     */
    public int getTotalIssues() {
        return errors.size() + warnings.size();
    }
}
