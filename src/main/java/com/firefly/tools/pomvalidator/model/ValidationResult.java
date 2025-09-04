/*
 * Copyright 2025 Firefly Software Solutions Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package com.firefly.tools.pomvalidator.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the result of POM validation containing errors, warnings, informational messages, and suggestions.
 */
public class ValidationResult {
    
    private final List<ValidationIssue> errors;
    private final List<ValidationIssue> warnings;
    private final List<ValidationIssue> infos;
    
    public ValidationResult() {
        this.errors = new ArrayList<>();
        this.warnings = new ArrayList<>();
        this.infos = new ArrayList<>();
    }
    
    // Getters
    public List<ValidationIssue> getErrors() {
        return errors;
    }
    
    public List<ValidationIssue> getWarnings() {
        return warnings;
    }
    
    public List<ValidationIssue> getInfos() {
        return infos;
    }
    
    // Add individual issues
    public void addError(ValidationIssue error) {
        this.errors.add(error);
    }
    
    public void addWarning(ValidationIssue warning) {
        this.warnings.add(warning);
    }
    
    public void addInfo(ValidationIssue info) {
        this.infos.add(info);
    }
    
    // Add multiple issues
    public void addErrors(List<ValidationIssue> errors) {
        this.errors.addAll(errors);
    }
    
    public void addWarnings(List<ValidationIssue> warnings) {
        this.warnings.addAll(warnings);
    }
    
    public void addInfos(List<ValidationIssue> infos) {
        this.infos.addAll(infos);
    }
    
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
    
    // Builder pattern for compatibility
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private final ValidationResult result = new ValidationResult();
        
        public Builder error(ValidationIssue error) {
            result.addError(error);
            return this;
        }
        
        public Builder warning(ValidationIssue warning) {
            result.addWarning(warning);
            return this;
        }
        
        public Builder info(ValidationIssue info) {
            result.addInfo(info);
            return this;
        }
        
        public ValidationResult build() {
            return result;
        }
    }
}
