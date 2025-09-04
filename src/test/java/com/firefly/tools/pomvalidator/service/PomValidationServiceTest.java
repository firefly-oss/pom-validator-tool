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


package com.firefly.tools.pomvalidator.service;

import com.firefly.tools.pomvalidator.model.ValidationResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class PomValidationServiceTest {
    
    @TempDir
    Path tempDir;
    
    @Test
    void shouldReturnErrorWhenPomFileDoesNotExist() throws Exception {
        PomValidationService service = new PomValidationService();
        Path nonExistentPom = tempDir.resolve("non-existent.xml");
        
        ValidationResult result = service.validatePom(nonExistentPom);
        
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors().get(0).getMessage()).contains("POM file does not exist");
    }
    
    @Test
    void shouldReturnErrorWhenPathIsNotFile() throws Exception {
        PomValidationService service = new PomValidationService();
        Path directory = tempDir.resolve("directory");
        Files.createDirectory(directory);
        
        ValidationResult result = service.validatePom(directory);
        
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors().get(0).getMessage()).contains("Path is not a regular file");
    }
    
    @Test
    void shouldReturnErrorWhenPomIsInvalid() throws Exception {
        PomValidationService service = new PomValidationService();
        Path invalidPom = tempDir.resolve("invalid-pom.xml");
        Files.writeString(invalidPom, "invalid xml content");
        
        ValidationResult result = service.validatePom(invalidPom);
        
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).isNotEmpty();
    }
    
    @Test
    void shouldValidateMinimalPom() throws Exception {
        PomValidationService service = new PomValidationService();
        Path validPom = tempDir.resolve("valid-pom.xml");
        
        String pomContent = """
            <?xml version="1.0" encoding="UTF-8"?>
            <project xmlns="http://maven.apache.org/POM/4.0.0"
                     xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                     xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
                     http://maven.apache.org/xsd/maven-4.0.0.xsd">
                <modelVersion>4.0.0</modelVersion>
                <groupId>com.example</groupId>
                <artifactId>test-project</artifactId>
                <version>1.0.0</version>
            </project>
            """;
        
        Files.writeString(validPom, pomContent);
        
        ValidationResult result = service.validatePom(validPom);
        
        // Should be valid (no errors) but might have warnings/info
        assertThat(result.isValid()).isTrue();
        assertThat(result.getErrors()).isEmpty();
    }
}
