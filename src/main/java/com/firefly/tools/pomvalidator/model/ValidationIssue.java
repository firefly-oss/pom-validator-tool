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
