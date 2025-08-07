package com.catalis.tools.pomvalidator.model;

import lombok.Data;
import lombok.Builder;

/**
 * Represents a single validation issue with a message and optional fix suggestion.
 */
@Data
@Builder
public class ValidationIssue {
    
    /**
     * The issue description message.
     */
    private final String message;
    
    /**
     * Optional suggestion for fixing the issue.
     */
    private final String suggestion;
    
    /**
     * Creates a simple issue with just a message.
     */
    public static ValidationIssue of(String message) {
        return ValidationIssue.builder()
            .message(message)
            .build();
    }
    
    /**
     * Creates an issue with a message and fix suggestion.
     */
    public static ValidationIssue of(String message, String suggestion) {
        return ValidationIssue.builder()
            .message(message)
            .suggestion(suggestion)
            .build();
    }
    
    /**
     * Returns true if this issue has a fix suggestion.
     */
    public boolean hasSuggestion() {
        return suggestion != null && !suggestion.trim().isEmpty();
    }
}
